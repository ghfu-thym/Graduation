import { DynamoDBClient } from "@aws-sdk/client-dynamodb";
import { DynamoDBDocumentClient, QueryCommand, UpdateCommand } from "@aws-sdk/lib-dynamodb";
import jwt from "jsonwebtoken";
import Redis from "ioredis";

// TỐI ƯU HẠ TẦNG
process.env.AWS_NODEJS_CONNECTION_REUSE_ENABLED = "1";

const docClient = DynamoDBDocumentClient.from(new DynamoDBClient({}));
const REDIS_URL = process.env.REDIS_URL || "redis://10.0.1.55:6379";
const redisClient = new Redis(REDIS_URL);
const PRIVATE_KEY = process.env.RSA_PRIVATE_KEY ? process.env.RSA_PRIVATE_KEY.replace(/\\n/g, '\n') : '';

export const handler = async (event) => {
    if (!PRIVATE_KEY) throw new Error("Missing RSA_PRIVATE_KEY");

    const eventId = String(event.eventId);
    const shardCount = Number(event.shardCount) || 1;
    const allocatedQuota = Number(event.allocatedQuota) || 50;

    console.log(`[Bắt đầu xả hàng] Event: ${eventId} | Shards: ${shardCount} | Quota: ${allocatedQuota}`);

    try {
        // --- 1. GOM DATA TỪ CÁC SHARD ---
        const limitPerShard = Math.ceil(allocatedQuota / shardCount);
        const queryPromises = [];

        for (let i = 1; i <= shardCount; i++) {
            const gsiQueueKey = `WAITING#${eventId}#shard_${i}`;
            queryPromises.push(docClient.send(new QueryCommand({
                TableName: "VirtualWaitingRoom",
                IndexName: "QueueSortingIndex",
                KeyConditionExpression: "gsiQueueKey = :qKey",
                ExpressionAttributeValues: { ":qKey": gsiQueueKey },
                Limit: limitPerShard
            })));
        }

        const results = await Promise.all(queryPromises);

        let allWaitingUsers = [];
        results.forEach(res => {
            if (res.Items && res.Items.length > 0) allWaitingUsers.push(...res.Items);
        });

        if (allWaitingUsers.length === 0) {
            console.log(`Hàng đợi trống. Lật cờ NORMAL.`);
            await docClient.send(new UpdateCommand({
                TableName: "EventConfig",
                Key: { eventId: eventId },
                UpdateExpression: "SET vwr_status = :st",
                ExpressionAttributeValues: { ":st": "NORMAL" }
            }));
            await redisClient.set(`serving:${eventId}`, 0);
            await redisClient.set(`ticket:${eventId}`, 0);
            return { statusCode: 200, body: "Queue empty." };
        }

        // Sắp xếp theo thời gian (FIFO) và cắt đúng Quota
        allWaitingUsers.sort((a, b) => a.timestamp - b.timestamp);
        const usersToProcess = allWaitingUsers.slice(0, allocatedQuota);

        // --- 2. ĐÚC TOKEN VÀ CẬP NHẬT DATABASE ---
        const updateDbPromises = [];

        for (const user of usersToProcess) {
            // Đúc Token RS256
            const passToken = jwt.sign(
                { sub: user.visitorToken, eventId: eventId, vwr_passed: true }, 
                PRIVATE_KEY, 
                { algorithm: 'RS256', expiresIn: '5m' }
            );

            // Cập nhật Database: Lưu token, đổi status, xóa Shard Index
            const updateParams = {
                TableName: "VirtualWaitingRoom",
                Key: { visitorToken: user.visitorToken },
                UpdateExpression: "SET #st = :allowed, accessToken = :token REMOVE gsiQueueKey",
                ExpressionAttributeNames: { "#st": "status" },
                ExpressionAttributeValues: { 
                    ":allowed": "ALLOWED",
                    ":token": passToken // Ghi token vào DB để Stream đẩy sang Notifier
                }
            };
            updateDbPromises.push(docClient.send(new UpdateCommand(updateParams)));
        }

        await Promise.all(updateDbPromises);

        // --- 3. NHẢY SỐ TRÊN REDIS ---
        await redisClient.incrby(`serving:${eventId}`, usersToProcess.length);
        console.log(`Đã xả ${usersToProcess.length} user. Hệ thống sẵn sàng.`);

        return { statusCode: 200, body: `Đã xả ${usersToProcess.length} user.` };

    } catch (error) {
        console.error("Lỗi Exchanger:", error);
        throw error;
    }
};