import { ApiGatewayManagementApiClient, PostToConnectionCommand } from "@aws-sdk/client-apigatewaymanagementapi";

// BẬT GIỮ KẾT NỐI ĐỂ BẮN WEBSOCKET TỐC ĐỘ CAO
process.env.AWS_NODEJS_CONNECTION_REUSE_ENABLED = "1";

// Dán URL API Gateway của bạn vào đây (Xóa phần /@connections ở cuối nếu có)
const API_ENDPOINT = "https://tlw4uxxvsd.execute-api.ap-southeast-1.amazonaws.com/dev";
const apiGwClient = new ApiGatewayManagementApiClient({ endpoint: API_ENDPOINT });

export const handler = async (event) => {
    const wsPromises = [];

    // Lặp qua các lô dữ liệu thay đổi từ DynamoDB Stream
    for (const record of event.Records) {
        if (record.eventName === 'MODIFY') {
            const newImage = record.dynamodb.NewImage;
            const oldImage = record.dynamodb.OldImage;

            const wasWaiting = oldImage?.status?.S === 'WAITING';
            const isNowAllowed = newImage?.status?.S === 'ALLOWED';
            
            // Nếu người này vừa được Exchanger "bật đèn xanh"
            if (wasWaiting && isNowAllowed) {
                const connectionId = newImage?.connectionId?.S;
                const passToken = newImage?.accessToken?.S; // Lấy Pass Token do Exchanger vừa đúc
                const eventId = newImage?.eventId?.S;

                if (connectionId && passToken) {
                    const payload = JSON.stringify({
                        action: "ticket_granted",
                        eventId: eventId,
                        passToken: passToken,
                        message: "Đến lượt bạn rồi! Đang chuyển hướng sang trang thanh toán..."
                    });

                    // Đẩy lệnh gửi tin nhắn vào mảng Promise
                    const command = new PostToConnectionCommand({
                        ConnectionId: connectionId,
                        Data: Buffer.from(payload)
                    });

                    // Dùng Promise để bắn bất đồng bộ, bọc try/catch để không chết lây
                    wsPromises.push(
                        apiGwClient.send(command).catch(error => {
                            if (error.$metadata?.httpStatusCode === 410) {
                                console.log(`[Gone] User ${connectionId} đã đóng trình duyệt.`);
                            } else {
                                console.error(`[Lỗi WS] Không thể gửi cho ${connectionId}:`, error);
                            }
                        })
                    );
                }
            }
        }
    }

    // Thực thi bắn hàng loạt WebSocket cùng 1 lúc (Cực kỳ tốc độ)
    await Promise.all(wsPromises);
    
    return { statusCode: 200, body: 'Đã xử lý Stream gửi Notifier.' };
};