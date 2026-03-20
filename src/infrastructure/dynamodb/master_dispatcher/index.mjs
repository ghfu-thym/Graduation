import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient, QueryCommand } from "@aws-sdk/lib-dynamodb";
import { LambdaClient, InvokeCommand } from "@aws-sdk/client-lambda";

// 1. TỐI ƯU HẠ TẦNG
process.env.AWS_NODEJS_CONNECTION_REUSE_ENABLED = "1";

const docClient = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const lambdaClient = new LambdaClient({}); // Khởi tạo Client để gọi Lambda khác

// BẮT BUỘC SỬA: Tên hàm Exchanger của bạn trên AWS (Ví dụ: "VWR_Exchanger_Function")
const EXCHANGER_FUNCTION_NAME = process.env.EXCHANGER_FUNCTION_NAME || "arn:aws:lambda:ap-southeast-1:123456789:function:VWR_Exchanger";

// Hàm logic cốt lõi: Quét DB và gọi Exchanger
const scanAndDispatch = async () => {
    try {
        console.log(`[${new Date().toISOString()}] Bắt đầu nhịp quét sự kiện...`);

        // 1. Quét GSI StatusIndex để tìm các sự kiện đang báo động (QUEUING)
        const queryParams = {
            TableName: "EventConfig",
            IndexName: "StatusIndex",
            KeyConditionExpression: "vwr_status = :st",
            ExpressionAttributeValues: { ":st": "QUEUING" }
        };

        const { Items } = await docClient.send(new QueryCommand(queryParams));

        if (!Items || Items.length === 0) {
            console.log("Không có sự kiện nào đang kẹt. Bỏ qua nhịp này.");
            return;
        }

        console.log(`Phát hiện ${Items.length} sự kiện đang kẹt. Bắt đầu điều phối...`);

        // 2. Lặp qua từng sự kiện kẹt và gọi Lambda Worker (Exchanger)
        const invokePromises = [];

        for (const eventConfig of Items) {
            // Chuẩn bị túi hành trang (Payload) giao cho Exchanger
            const payloadToWorker = {
                eventId: eventConfig.eventId,
                shardCount: eventConfig.shardCount || 1,
                // Lấy batchSize từ cấu hình (Ví dụ: xả 50 người/nhịp)
                allocatedQuota: eventConfig.batchSize || 50 
            };

            // Lệnh gọi Lambda khác (CÚ CHỐT NẰM Ở InvocationType)
            const invokeCommand = new InvokeCommand({
                FunctionName: EXCHANGER_FUNCTION_NAME,
                // 'Event' nghĩa là gọi Bất đồng bộ (Asynchronous). 
                // Dispatcher chỉ ném payload vào hàng đợi nội bộ của Lambda rồi đi luôn, không chờ kết quả trả về.
                InvocationType: "Event", 
                Payload: Buffer.from(JSON.stringify(payloadToWorker))
            });

            invokePromises.push(lambdaClient.send(invokeCommand));
            console.log(`Đã phát lệnh gọi Exchanger cho Event: ${eventConfig.eventId}`);
        }

        // Bắn lệnh đồng loạt
        await Promise.all(invokePromises);

    } catch (error) {
        console.error("Lỗi trong quá trình quét và điều phối:", error);
    }
};

// HANDLER CHÍNH ĐƯỢC KÍCH HOẠT BỞI EVENTBRIDGE (Mỗi 1 phút / lần)
export const handler = async (event) => {
    console.log("Master Dispatcher được EventBridge đánh thức. Bắt đầu chu kỳ 60 giây...");

    const LOOPS = 6;            // Chạy 6 nhịp
    const DELAY_MS = 10000;     // Mỗi nhịp cách nhau 10 giây

    for (let i = 0; i < LOOPS; i++) {
        // Chạy nghiệp vụ quét
        await scanAndDispatch();

        // Ngủ đông 10 giây (trừ vòng lặp cuối cùng để tránh quá thời gian 60s của cron)
        if (i < LOOPS - 1) {
            await new Promise(resolve => setTimeout(resolve, DELAY_MS));
        }
    }

    console.log("Kết thúc chu kỳ 60 giây. Chờ EventBridge gọi lại vào phút tiếp theo.");
    return { statusCode: 200, body: 'Chu kỳ điều phối hoàn tất.' };
};