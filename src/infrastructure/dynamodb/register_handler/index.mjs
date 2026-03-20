import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient, PutCommand, GetCommand, UpdateCommand } from "@aws-sdk/lib-dynamodb";
import { ApiGatewayManagementApiClient, PostToConnectionCommand } from "@aws-sdk/client-apigatewaymanagementapi";
import jwt from 'jsonwebtoken';
import Redis from "ioredis";

// 1. TỐI ƯU HẠ TẦNG: Giữ kết nối TCP sống sót (Chống nghẽn card mạng EC2)
process.env.AWS_NODEJS_CONNECTION_REUSE_ENABLED = "1";

// 2. CẤU HÌNH ĐẦU MỐI AWS
const client = new DynamoDBClient({});
const docClient = DynamoDBDocumentClient.from(client);

// BẮT BUỘC SỬA 1: URL WebSocket API Gateway của bạn (Xóa đuôi /@connections nếu có)
const API_ENDPOINT = "https://tlw4uxxvsd.execute-api.ap-southeast-1.amazonaws.com/dev";
const apiGwClient = new ApiGatewayManagementApiClient({ endpoint: API_ENDPOINT });

// BẮT BUỘC SỬA 2: URL con EC2 chạy Redis của bạn
const REDIS_URL = process.env.REDIS_URL || "redis://:doan2026@47.129.29.189"; 

// 3. KHỞI TẠO REDIS BÊN NGOÀI HANDLER (Chìa khóa để tái sử dụng kết nối)
const redisClient = new Redis(REDIS_URL);

export const handler = async (event) => {
    // Lấy ID của đường ống WebSocket hiện tại để lát nữa trả lời đúng người
    const connectionId = event.requestContext.connectionId;
    
    try {
        // Parse dữ liệu Frontend gửi lên qua tin nhắn WebSocket
        const body = JSON.parse(event.body);
        
        // --- BƯỚC 1: XỬ LÝ DỮ LIỆU ĐẦU VÀO ---
        // Frontend gửi lên JWT gốc (của Spring Boot)
        const authToken = body.visitorToken; 
        
        // Ép kiểu eventId thành String một cách an toàn (Ví dụ: số 1 -> chuỗi "1")
        // Điều này đảm bảo DynamoDB không bị lỗi Data Type
        if (!body.eventId) throw new Error("Thiếu eventId");
        const eventId = String(body.eventId); 
        
        // Lấy số Shard do Director báo về (hoặc mặc định là 1 nếu lỗi)
        const shardCount = Number(body.shardCount) || 1; 

        // --- BƯỚC 2: XÁC THỰC NGƯỜI DÙNG ---
        // Chỉ cần decode để lấy userId (không cần verify chữ ký vì đã verify ở Spring Boot lúc login)
        const decoded = jwt.decode(authToken);
        if (!decoded || !decoded.sub) {
            console.warn("Token không hợp lệ hoặc thiếu sub:", authToken);
            return { statusCode: 400, body: 'Token định danh không hợp lệ' };
        }
        
        // Spring Boot thường lưu userId ở trường 'sub' (Subject)
        const userId = String(decoded.sub);

        let ticketNumber;
        let timestamp;

        // --- BƯỚC 3: KIỂM TRA LỊCH SỬ XẾP HÀNG (RECONNECT LOGIC) ---
        const existingUser = await docClient.send(new GetCommand({
            TableName: "VirtualWaitingRoom",
            Key: { visitorToken: userId } 
        }));

        if (existingUser.Item && existingUser.Item.status === "WAITING") {
            // KỊCH BẢN A: Người dùng F5 trình duyệt hoặc rớt mạng kết nối lại
            ticketNumber = existingUser.Item.ticketNumber;
            timestamp = existingUser.Item.timestamp; // Giữ nguyên mốc thời gian cũ để không mất chỗ

            // Cập nhật ống connectionId mới và gia hạn thời gian sống (TTL) thêm 15 phút
            await docClient.send(new UpdateCommand({
                TableName: "VirtualWaitingRoom",
                Key: { visitorToken: userId },
                UpdateExpression: "SET connectionId = :cid, #ttl = :ttl",
                ExpressionAttributeNames: { "#ttl": "ttl" },
                ExpressionAttributeValues: {
                    ":cid": connectionId,
                    ":ttl": Math.floor(Date.now() / 1000) + 900 
                }
            }));
            
            console.log(`[Tái kết nối] User: ${userId}, Event: ${eventId}, Vị trí gốc: ${ticketNumber}`);
            
        } else {
            // KỊCH BẢN B: Người dùng mới tinh, bắt đầu bốc số
            timestamp = Date.now();

            // 1. Chọc thẳng vào Redis EC2 để xin số (Rất nhanh, không lo Hot Partition)
            // Khóa Redis sẽ có dạng: ticket:1
            ticketNumber = await redisClient.incr(`ticket:${eventId}`);

            // 2. Tung xúc xắc chọn Shard để lách giới hạn ghi của DynamoDB
            const randomShard = Math.floor(Math.random() * shardCount) + 1;
            
            // Khóa GSI chịu trách nhiệm phân mảnh (Ví dụ: WAITING#1#shard_5)
            const gsiQueueKey = `WAITING#${eventId}#shard_${randomShard}`;

            // 3. Ghi dữ liệu vào DynamoDB
            await docClient.send(new PutCommand({
                TableName: "VirtualWaitingRoom",
                Item: {
                    visitorToken: userId,        // Khóa chính: Phân tán tự nhiên
                    eventId: eventId,            // Lưu để dễ audit
                    connectionId: connectionId,  // Lưu đường ống để Exchanger gọi
                    gsiQueueKey: gsiQueueKey,    // Khóa GSI: Quyết định vách ngăn vật lý
                    timestamp: timestamp,        // Khóa sắp xếp GSI: Chuẩn FIFO
                    status: "WAITING",
                    ticketNumber: ticketNumber,  // Số thứ tự cho Frontend
                    ttl: Math.floor(Date.now() / 1000) + 900 // Tự dọn rác sau 15 phút
                }
            }));
            
            console.log(`[Vào hàng] User: ${userId}, Event: ${eventId}, Shard: ${randomShard}, Số: ${ticketNumber}`);
        }

        // --- BƯỚC 4: TRẢ KẾT QUẢ VỀ FRONTEND QUA WEBSOCKET ---
        const responsePayload = JSON.stringify({
            action: "registered_success",
            eventId: eventId,
            ticketNumber: ticketNumber,
            message: "Bạn đã vào hàng đợi thành công"
        });

        await apiGwClient.send(new PostToConnectionCommand({
            ConnectionId: connectionId,
            Data: Buffer.from(responsePayload)
        }));

        return { statusCode: 200, body: 'Đã xử lý' };

    } catch (err) {
        console.error("Lỗi kịch liệt tại Register:", err);
        
        // Nếu lỗi, cố gắng bắn thông báo về cho Frontend biết
        try {
            await apiGwClient.send(new PostToConnectionCommand({
                ConnectionId: connectionId,
                Data: Buffer.from(JSON.stringify({ action: "error", message: "Hệ thống xếp hàng đang bận, đang thử lại..." }))
            }));
        } catch (e) { /* Bỏ qua nếu ống kết nối đã chết */ }

        return { statusCode: 500, body: 'Lỗi máy chủ' };
    }
};