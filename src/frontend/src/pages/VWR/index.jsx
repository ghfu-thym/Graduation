import React, { useEffect, useMemo, useRef, useState } from "react";

const CLOUDFRONT_API_URL =
  import.meta.env.VITE_VWR_CLOUDFRONT_API_URL ||
  "https://d1cpe6xn6cl1ii.cloudfront.net/api/v1/ticket-gate/1";
const WEBSOCKET_URL =
  import.meta.env.VITE_VWR_WEBSOCKET_URL ||
  "wss://dlhievog91.execute-api.ap-southeast-1.amazonaws.com/test/";
const DEFAULT_LOGIN_TOKEN =
  import.meta.env.VITE_VWR_LOGIN_TOKEN ||
  "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6IlVTRVIiLCJlbWFpbCI6InNwaWtlLnVzZXJAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InNwaWtlX3VzZXIiLCJpYXQiOjE3NzkxNTU2MDEsImV4cCI6MTc4MTc0NzYwMX0.Je2KmjTczKFVtUGBUjJwlwSSgyheUjdK_PNJZD4tz1vermmkdv4vLMK5cV2e52trBhzO0rhqm5FIwLKuUnYmX0y_rsX3CPS-Wj-41HJFHSNznXLBlS0KMZMy5X-x8PUZ-vbhQssEBibvZa-C3GXquEblJPH9oRT6XOtRDVtiNgDoqws0M0sLilaKVQSjF61Y8QCILS2gb3MPfb7lXwI7DNsibHU6OitaFqQg-c0d4dKVKtRTEBOb0YNmhdkKbc0O4Nmz9Pv4ZxdyQzHWMCkDBy1DK5TwU9RMTOyppV753tLAy4Eitj170GA1tiuqJt3UtgNoE156x0QViNaOFGfbGQ";

const Vwr = () => {
  const [screen, setScreen] = useState("initial");
  const [queueNumber, setQueueNumber] = useState("--");
  const [isPulsing, setIsPulsing] = useState(false);
  const [accessToken, setAccessToken] = useState("");
  const [errorMessage, setErrorMessage] = useState(
    "Rất tiếc, đã có lỗi xảy ra. Vui lòng thử lại sau."
  );

  const wsRef = useRef(null);
  const currentPositionRef = useRef(-1);

  const loginToken = useMemo(() => DEFAULT_LOGIN_TOKEN, []);

  useEffect(() => {
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
        wsRef.current = null;
      }
    };
  }, []);

  const showScreen = (next) => {
    setScreen(next);
  };

  const handleSuccess = (token) => {
    showScreen("success");
    setAccessToken(token);
    localStorage.setItem("vwr_pass_token", token);
  };

  const handleError = (message) => {
    showScreen("error");
    setErrorMessage(message);
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
  };

  const connectWebSocket = (visitorToken, eventId) => {
    const socketUrl = `${WEBSOCKET_URL}?visitorToken=${encodeURIComponent(
      visitorToken
    )}&eventId=${encodeURIComponent(eventId)}`;

    const ws = new WebSocket(socketUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      const registerPayload = {
        action: "register",
        visitorToken,
        eventId,
        shardCount: 1,
      };
      ws.send(JSON.stringify(registerPayload));
      console.log("sent to ws")
    };

    ws.onmessage = (event) => {
      try {
        const wsData = JSON.parse(event.data);

        if (wsData.action === "registered_success" && wsData.ticketNumber) {
          const newRank = wsData.ticketNumber;
          if (currentPositionRef.current !== newRank) {
            setQueueNumber(newRank);
            setIsPulsing(true);
            window.setTimeout(() => setIsPulsing(false), 500);
            currentPositionRef.current = newRank;
          }
        } else if (wsData.action === "token_granted" && wsData.accessToken) {
          handleSuccess(wsData.accessToken);
          ws.close();
        } else if (wsData.action === "error") {
          handleError(wsData.message || "Lỗi không xác định từ hàng đợi.");
        }
      } catch (error) {
        console.error("❌ Error parsing WS message:", error);
      }
    };

    ws.onerror = (err) => {
      console.error("❌ WebSocket Error:", err);
      handleError("Mất kết nối với hàng đợi. Vui lòng tải lại trang.");
    };
  };

  const startBuyingProcess = async () => {
    showScreen("loading");

    try {

      const response = await fetch(CLOUDFRONT_API_URL, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${loginToken}`
        }
      });

      const data = await response.json();

      if (response.status === 401 || data.action === "LOGIN_REQUIRED") {
        handleError("Bạn chưa đăng nhập. Không thể tiến hành mua vé.");
        return;
      }

      if (data.action === "BYPASS") {
        handleSuccess(data.accessToken);
        return;
      }

      if (data.action === "QUEUE" || response.status === 429) {
        showScreen("queue");
        connectWebSocket(loginToken, data.eventId || "1");
        return;
      }

      if (response.status === 403) {
        handleError(data.message || "Sự kiện hiện chưa mở bán.");
        return;
      }

      handleError("Không thể bắt đầu hàng đợi. Vui lòng thử lại.");
    } catch (error) {
      console.error("❌ Network Error:", error);
      handleError("Lỗi kết nối đến máy chủ. Vui lòng kiểm tra lại đường truyền.");
    }
  };

  return (
    <main className="flex-1 flex items-center justify-center bg-void text-primary px-6 py-12">
      <div className="card">
        <section className={`screen ${screen === "initial" ? "active" : ""}`}>
          <h2 className="font-display-light text-heading-4">🔥 Sự kiện Âm nhạc 2026 🔥</h2>
          <p className="text-body-small text-muted-text">
            Vé đang được bán ra với số lượng có hạn. Hãy sẵn sàng để giành lấy vị trí của bạn!
          </p>
          <button className="btn" type="button" onClick={startBuyingProcess}>
            ĐẶT VÉ NGAY
          </button>
        </section>

        <section className={`screen ${screen === "loading" ? "active" : ""}`}>
          <h2 className="font-display-light text-heading-4">Đang kết nối...</h2>
          <p className="text-body-small text-muted-text">
            Hệ thống đang xử lý yêu cầu của bạn. Vui lòng đợi trong giây lát.
          </p>
          <div className="loader-container">
            <div className="lds-ring">
              <div></div>
              <div></div>
              <div></div>
              <div></div>
            </div>
          </div>
        </section>

        <section className={`screen ${screen === "queue" ? "active" : ""}`}>
          <h2 className="font-display-light text-heading-4">🚧 BẠN ĐANG TRONG HÀNG ĐỢI 🚧</h2>
          <p className="text-body-small text-muted-text">
            Hệ thống đang quá tải. Vui lòng không tải lại trang. Chúng tôi sẽ tự động chuyển hướng bạn khi đến lượt.
          </p>
          <div className="queue-box">
            <div className="position-label">Vị trí của bạn</div>
            <div className={`position-number ${isPulsing ? "pulse" : ""}`}>
              {queueNumber}
            </div>
            <div className="status-badge">ĐANG CHỜ</div>
          </div>
        </section>

        <section className={`screen ${screen === "success" ? "active" : ""}`}>
          <h2 className="font-display-light text-heading-4">🎉 ĐÃ ĐẾN LƯỢT CỦA BẠN!</h2>
          <p className="text-body-small text-muted-text">
            Chúc mừng! Bạn đã có thể tiếp tục. Hệ thống đang chuyển hướng bạn đến trang đặt vé...
          </p>
          {accessToken && (
            <div className="token-box">
              <strong>Access Token:</strong> {accessToken.substring(0, 30)}...
            </div>
          )}
        </section>

        <section className={`screen ${screen === "error" ? "active" : ""}`}>
          <h2 className="font-display-light text-heading-4">⚠️ Đã xảy ra lỗi</h2>
          <p className="text-body-small text-muted-text">{errorMessage}</p>
          <button className="btn" type="button" onClick={() => window.location.reload()}>
            Tải lại trang
          </button>
        </section>
      </div>
    </main>
  );
};

export default Vwr;


