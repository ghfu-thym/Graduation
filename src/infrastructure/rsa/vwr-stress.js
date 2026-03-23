import ws from 'k6/ws';
import { check,sleep } from 'k6';
import { SharedArray } from 'k6/data';
import exec from 'k6/execution'; // IMPORT THƯ VIỆN QUẢN LÝ TIẾN TRÌNH

// 1. NẠP DỮ LIỆU DÙNG CHUNG (Chỉ đọc 1 lần vào RAM)
const tokens = new SharedArray('Visitor Tokens', function () {
    const fileContent = open('./tokens.csv');
    return fileContent.split('\n').filter(t => t.trim() !== '');
});

export const options = {
    scenarios: {
        queue_waiting_test: {
            executor: 'per-vu-iterations',
            vus: 50,           // Số lượng bot bạn muốn tung vào (Lưu ý: Không được vượt quá số Token trong file CSV)
            iterations: 1,     // MỖI BOT CHỈ CHẠY ĐÚNG 1 LẦN (Nhận vé xong là nghỉ)
            maxDuration: '5m', // Thời gian chờ TỐI ĐA cho toàn bộ chiến dịch (Ví dụ chờ lâu nhất là 5 phút)
        },
    },
};

const WS_URL = "wss://tlw4uxxvsd.execute-api.ap-southeast-1.amazonaws.com/dev";
const EVENT_ID = "1";

import ws from 'k6/ws';
import { check, sleep } from 'k6'; // NHỚ IMPORT THÊM lệnh sleep ở dòng trên cùng
import { SharedArray } from 'k6/data';
import exec from 'k6/execution'; 


export default function () {
    // 1. TẠO ĐỘ TRỄ NGẪU NHIÊN (JITTER)
    // Cho các bot xuất phát lệch nhau ngẫu nhiên từ 0.1s đến 3.0s để bảo vệ AWS API Gateway
    sleep(Math.random() * 3);

    // 2. Lấy Token theo ID của VU
    const uniqueIndex = exec.scenario.iterationInTest;
    const myToken = tokens[uniqueIndex % tokens.length];
    
    const url = `${WS_URL}?visitorToken=${myToken}&eventId=${EVENT_ID}`;
    const params = { tags: { my_tag: 'vwr_ws_test' } };

    // 3. Mở kết nối và HOLD TẢI (Hàm ws.connect của k6 tự động chặn/block kịch bản lại tại đây)
    const res = ws.connect(url, params, function (socket) {
        
        socket.on('open', function () {
            // console.log(`[VU ${exec.vu.idInTest}] Đã mở kết nối. Đang xin số...`);
            socket.send(JSON.stringify({
                action: "register",
                visitorToken: myToken,
                eventId: EVENT_ID,
                shardCount: 1
            }));
        });

        socket.on('message', function (msg) {
            try {
                const data = JSON.parse(msg);
                if (data.action === "token_granted" || data.passToken) {
                    // KHI NHẬN ĐƯỢC VÉ -> Đóng ống. Hàm ws.connect sẽ kết thúc, VU hoàn thành nhiệm vụ và nghỉ.
                    // console.log(`🎉 [VU ${exec.vu.idInTest}] NHẬN VÉ THÀNH CÔNG! Rời hàng đợi.`);
                    socket.close(); 
                }
            } catch (e) { }
        });

        // Tự động giải tán nếu Exchanger sập, tránh treo bot vĩnh viễn
        socket.setTimeout(function () {
            console.error(`[VU ${exec.vu.idInTest}] ⏳ QUÁ GIỜ (TIMEOUT)! Bỏ cuộc.`);
            socket.close();
        }, 300000); // Đợi tối đa 5 phút (300.000 ms)
    });

    check(res, {
        'Kết nối WS thành công (101)': (r) => r && r.status === 101,
    });
}