import Redis from "ioredis";
import jwt from "jsonwebtoken";
import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient, UpdateCommand } from "@aws-sdk/lib-dynamodb";

// BẬT TÍNH NĂNG TÁI SỬ DỤNG KẾT NỐI
process.env.AWS_NODEJS_CONNECTION_REUSE_ENABLED = "1";

// CẤU HÌNH HỆ THỐNG
const REDIS_URL = process.env.REDIS_URL || "redis://:doan2026@47.129.29.189";
const MAX_REQUESTS_PER_SECOND = 1;

// Xử lý Private Key y hệt như bên Exchanger và Notifier
let PRIVATE_KEY = process.env.PRIVATE_KEY || "";
PRIVATE_KEY = PRIVATE_KEY.replace(/\\n/g, '\n').trim();

// KHỞI TẠO BÊN NGOÀI HANDLER
const redisClient = new Redis(REDIS_URL);
const docClient = DynamoDBDocumentClient.from(new DynamoDBClient({ region: "ap-southeast-1" }));

export const handler = async (event) => {
    try {
        if (!PRIVATE_KEY) throw new Error("Thiếu RSA_PRIVATE_KEY");

        const eventIdRaw = event.pathParameters?.eventId;
        if (!eventIdRaw) return { statusCode: 400, body: JSON.stringify({ message: "Thiếu eventId" }) };
        
        const eventId = String(eventIdRaw);
        
        // --- BẮT ĐẦU LUỒNG KIỂM TRA CHỚP NHOÁNG ---
        
        const currentSecond = Math.floor(Date.now() / 1000);
        const redisKey = `load:${eventId}:${currentSecond}`;
        const statusKey = `vwr_status:${eventId}`; // Khóa chứa cờ báo bão trên Redis
        
        //Đọc cờ báo bão và Tăng xô cùng một lúc (Tốc độ ánh sáng)
        const results = await redisClient.multi()
            .get(statusKey)
            .incr(redisKey)
            .expire(redisKey, 5)
            .exec();
            
        const currentStatus = results[0][1]; // Trạng thái sự kiện (null hoặc "QUEUING")
        //const currentStatus = "QUEUING"; // BẬT CHẾ ĐỘ TEST BÃO LUÔN ĐẾN
        const currentLoad = results[1][1];   // Tải của giây hiện tại
        
        console.log(`[Event ${eventId}] Trạng thái: ${currentStatus || 'NORMAL'} | Tải: ${currentLoad}/${MAX_REQUESTS_PER_SECOND}`);

        // --- KIỂM TRA QUOTA BẰNG LOGIC MỚI ---
        
        // Điều kiện để được qua: CHƯA CÓ BÃO và XÔ CHƯA ĐẦY
        if (currentStatus !== "QUEUING" && currentLoad <= MAX_REQUESTS_PER_SECOND) {
            
            const passToken = jwt.sign(
                { sub: "user_identifier", eventId: eventId, vwr_passed: true }, 
                PRIVATE_KEY, 
                { algorithm: 'RS256', expiresIn: '5m' } 
            );

            return {
                statusCode: 200,
                headers: {
                    "Content-Type": "application/json",
                    "Cache-Control": "no-store, no-cache, must-revalidate" 
                },
                body: JSON.stringify({
                    action: "BYPASS",
                    passToken: passToken
                })
            };
        } else {
            // KỊCH BẢN B: XÔ ĐẦY HOẶC ĐANG TRONG BÃO -> ÉP XẾP HÀNG
            
            // Nếu đây là request đầu tiên làm vỡ xô (Cờ chưa được bật)
            if (currentStatus !== "QUEUING") {
                // 1. Cắm cờ "Báo đỏ" lên Redis ngay lập tức (Để khóa mõm các request ở giây tiếp theo)
                // Cài thời gian sống 60 giây. Nếu bão kéo dài, các request sau sẽ liên tục gia hạn nó.
                await redisClient.set(statusKey, "QUEUING", "EX", 60);
                
                // 2. Fix Thundering Herd: Bầu ra 1 request duy nhất đi báo cáo lên DynamoDB
                const lockKey = `lock:db_update:${eventId}`;
                const isFirstToNoticeStorm = await redisClient.set(lockKey, "LOCKED", "EX", 5, "NX");
                
                if (isFirstToNoticeStorm) {
                    console.log(`[Event ${eventId}] BÃO ĐẾN! Chốt sổ và báo cho DynamoDB...`);
                    try {
                        await docClient.send(new UpdateCommand({
                            TableName: "EventConfig",
                            Key: { eventId: eventId },
                            UpdateExpression: "SET vwr_status = :status",
                            ExpressionAttributeValues: { ":status": "QUEUING" }
                        }));
                    } catch (dbErr) {
                        console.error("Lỗi cập nhật DynamoDB:", dbErr);
                    }
                }
            } else {
                // Nếu cờ đã là QUEUING, ta gia hạn thêm TTL để cờ không bị mất giữa chừng khi bão còn mạnh
                // Cứ có người bị chặn là cờ lại được gia hạn thêm 60s
                await redisClient.expire(statusKey, 60);
            }

            return {
                statusCode: 429,
                headers: {
                    "Content-Type": "application/json",
                    "Cache-Control": "max-age=5" 
                },
                body: JSON.stringify({
                    action: "QUEUE",
                    message: "Hệ thống đang xử lý tải cao, vui lòng xếp hàng.",
                    eventId: eventId,
                    shardCount: 1 // Gửi shard mặc định (hoặc bạn có thể cache từ DB lên Redis để linh hoạt)
                })
            };
        }

    } catch (err) {
        console.error("Lỗi Director:", err);
        return { statusCode: 500, body: JSON.stringify({ message: "Lỗi Server" }) };
    }
};