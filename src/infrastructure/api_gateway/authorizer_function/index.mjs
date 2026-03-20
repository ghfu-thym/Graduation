import jwt from 'jsonwebtoken';

const PUBLIC_KEY = process.env.RSA_PUBLIC_KEY ? process.env.RSA_PUBLIC_KEY.replace(/\\n/g, '\n') : '';

export const handler = async (event) => {
    const authHeader = event.headers.authorization || event.headers.Authorization;
    
    if (!authHeader || !authHeader.startsWith("Bearer ")) {
        return { isAuthorized: false };
    }

    const token = authHeader.split(" ")[1];

    try {
        const decoded = jwt.verify(token, PUBLIC_KEY, { algorithms: ['RS256'] });
        
        // 1. Kiểm tra cờ phòng chờ ảo
        if (decoded.vwr_passed !== true) {
            console.error(`[CHẶN] User ${decoded.sub} chưa qua phòng chờ ảo!`);
            return { isAuthorized: false };
        }
        
        // 2. Trả về true để API Gateway chuyển tiếp nguyên vẹn Request xuống Spring Boot
        return { isAuthorized: true };

    } catch (error) {
        console.error("Token không hợp lệ:", error.message);
        return { isAuthorized: false };
    }
};