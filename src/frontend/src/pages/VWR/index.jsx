import React, { useEffect, useMemo, useRef, useState } from "react";

const TICKET_GATE_BASE_URL =
  import.meta.env.VITE_VWR_TICKET_GATE_URL ||
  "https://d1cpe6xn6cl1ii.cloudfront.net/api/v1/ticket-gate";
const WEBSOCKET_URL =
  import.meta.env.VITE_VWR_WEBSOCKET_URL ||
  "wss://dlhievog91.execute-api.ap-southeast-1.amazonaws.com/test/";

const Vwr = ({ eventId }) => {
  const [screen, setScreen] = useState("initial");
  const [accessToken, setAccessToken] = useState("");
  const [errorMessage, setErrorMessage] = useState(
    "Rất tiếc, đã có lỗi xảy ra. Vui lòng thử lại sau."
  );

  const wsRef = useRef(null);
  const currentPositionRef = useRef(-1);
  const resolvedEventId = eventId || "1";
  const authToken = useMemo(() => localStorage.getItem("authToken") || "", []);
  const eventSnapshot = useMemo(() => {
    try {
      const raw = localStorage.getItem("vwr_event_snapshot");
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }, []);
  const resolvedShardCount = useMemo(() => {
    const rawCount = eventSnapshot?.shardCount;
    const parsed = Number(rawCount);
    if (!Number.isFinite(parsed) || parsed <= 0) return 1;
    return Math.floor(parsed);
  }, [eventSnapshot]);

  const eventTitle = eventSnapshot?.name || "Sự kiện đang chờ";
  const eventLocation = eventSnapshot?.location || "";
  const eventStartTime = eventSnapshot?.startTime || "";
  const eventImage = eventSnapshot?.imageUrl || "";

  const formattedStartTime = useMemo(() => {
    if (!eventStartTime) return "";
    const date = new Date(eventStartTime);
    if (Number.isNaN(date.getTime())) return eventStartTime;

    return new Intl.DateTimeFormat("vi-VN", {
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date);
  }, [eventStartTime]);

  useEffect(() => {
    if (!authToken) {
      localStorage.setItem("postLoginRedirect", `#/vwr/${resolvedEventId}`);
      window.location.hash = "#/login";
    }
  }, [authToken, resolvedEventId]);

  useEffect(() => {
    if (authToken) {
      startBuyingProcess();
    }
  }, [authToken]);

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
    console.log("Access token received:", token);
    localStorage.setItem("vwr_pass_token", token);
    window.setTimeout(() => {
      window.location.hash = `#/choose-ticket/${resolvedEventId}`;
    }, 600);
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
        shardCount: resolvedShardCount,
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
            currentPositionRef.current = newRank;
          }
        } else if (wsData.action === "ticket_granted" && wsData.passToken) {
          handleSuccess(wsData.passToken);
          ws.close();
          console.log("received from ws")
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
    if (!authToken) {
      localStorage.setItem("postLoginRedirect", `#/vwr/${resolvedEventId}`);
      window.location.hash = "#/login";
      return;
    }

    showScreen("loading");

    try {
      const response = await fetch(`${TICKET_GATE_BASE_URL}/${resolvedEventId}`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${authToken}`,
        },
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
        connectWebSocket(authToken, resolvedEventId);
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
    <main className="flex-1 bg-gray-50 text-gray-900 min-h-screen">
      <section className="pt-[80px] pb-8 px-6 md:px-8 max-w-[1280px] mx-auto w-full flex flex-col items-center">
        <div className="w-full max-w-3xl flex flex-col gap-space-4 mt-4 md:mt-6">
          <section className="flex flex-col md:flex-row items-center gap-space-4 md:gap-space-6 p-space-4 md:p-space-5 rounded-[24px] border border-gray-200 bg-white shadow-lg w-full">
            {eventImage ? (
              <img
                alt={eventTitle}
                className="w-20 h-20 md:w-28 md:h-28 rounded-2xl object-cover border border-gray-200 shadow-md shrink-0"
                src={eventImage}
              />
            ) : (
              <div className="w-20 h-20 md:w-28 md:h-28 rounded-2xl bg-gray-100 border border-gray-200 shadow-md shrink-0" />
            )}
            <div className="flex flex-col gap-space-2 text-center md:text-left">
              <h2 className="font-heading-3 text-[32px] md:text-heading-3 text-gray-900 leading-tight">
                {eventTitle}
              </h2>
              <p className="font-body-large text-body-large text-gray-500 flex items-center justify-center md:justify-start gap-space-2 mt-2">
                <span className="material-symbols-outlined text-[24px]">calendar_month</span>
                {formattedStartTime || "Đang cập nhật"}
                {eventLocation ? ` • ${eventLocation}` : ""}
              </p>
            </div>
          </section>

          <section className="relative bg-white rounded-[12px] p-space-5 md:p-space-6 border border-gray-200 shadow-xl flex flex-col items-center text-center gap-space-4 overflow-hidden w-full max-w-2xl mx-auto">
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[80%] h-[150px] bg-secondary-fixed/10 blur-[80px] rounded-full pointer-events-none"></div>
            <h1 className="font-display-light text-[32px] md:text-[40px] text-gray-900 tracking-tight leading-tight z-10">
              Bạn đang ở trong<br />hàng đợi
            </h1>
            <div className="relative w-20 h-20 flex items-center justify-center my-space-1 z-10">
              <div className="absolute inset-0 rounded-full border-2 border-gray-200 opacity-50"></div>
              <div
                className="absolute inset-0 rounded-full border-t-2 border-r-2 border-neon-green hourglass-ring-spin"
                style={{ transform: "rotate(45deg)" }}
              ></div>
              <div className="absolute inset-0 rounded-full bg-neon-green/5 blur-xl"></div>
              <span
                className="material-symbols-outlined text-4xl text-neon-green"
                style={{ fontVariationSettings: "'FILL' 0" }}
              >
                hourglass_empty
              </span>
            </div>

            {screen === "loading" && (
              <p className="font-body-standard text-body-standard text-gray-600 z-10">
                Đang kết nối, vui lòng đợi trong giây lát...
              </p>
            )}

            {screen === "queue" && (
              <div className="w-full mt-space-2 p-space-4 rounded-lg border border-gray-200 bg-gray-50 flex gap-space-3 text-left items-start z-10">
                <span className="material-symbols-outlined text-gray-500 shrink-0" style={{ fontVariationSettings: "'FILL' 1" }}>
                  info
                </span>
                <p className="font-body-standard text-body-standard text-gray-700">
                  Vui lòng không tải lại (refresh) trang. Khi đến lượt, bạn sẽ có 10 phút để hoàn tất mua vé.
                </p>
              </div>
            )}

            {screen === "success" && (
              <div className="w-full mt-space-2 p-space-4 rounded-lg border border-emerald-100 bg-emerald-50 text-left z-10">
                <p className="font-body-medium text-body-medium text-emerald-700">
                  Chúc mừng! Bạn đã có thể tiếp tục. Hệ thống đang chuyển hướng bạn đến trang đặt vé...
                </p>
              </div>
            )}

            {screen === "error" && (
              <div className="w-full mt-space-2 p-space-4 rounded-lg border border-red-200 bg-red-50 text-left z-10">
                <p className="font-body-medium text-body-medium text-red-600">{errorMessage}</p>
              </div>
            )}
          </section>
        </div>
      </section>
    </main>
  );
};

export default Vwr;

