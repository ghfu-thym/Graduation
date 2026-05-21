import React, { useEffect, useMemo, useState } from "react";

const formatCurrency = (value) => {
  if (value === null || value === undefined) return "";
  return new Intl.NumberFormat("vi-VN").format(value);
};

const formatDateTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "long",
    year: "numeric",
  }).format(date);
};

const ChooseTicket = () => {
  const [quantities, setQuantities] = useState({});

  const eventSnapshot = useMemo(() => {
    try {
      const raw = localStorage.getItem("vwr_event_snapshot");
      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }, []);

  const ticketCategories = useMemo(() => {
    if (!eventSnapshot?.categoryItemList?.length) return [];
    return eventSnapshot.categoryItemList;
  }, [eventSnapshot]);

  useEffect(() => {
    if (!ticketCategories.length) return;
    setQuantities((prev) => {
      const next = { ...prev };
      ticketCategories.forEach((category, index) => {
        const key = String(category.id ?? category.name ?? index);
        if (typeof next[key] !== "number") {
          next[key] = 0;
        }
      });
      return next;
    });
  }, [ticketCategories]);

  const handleAdjust = (category, delta) => {
    const key = String(category.id ?? category.name);
    const current = quantities[key] ?? 0;
    const next = current + delta;
    const max = typeof category.quantity === "number" ? category.quantity : Number(category.quantity);
    const safeMax = Number.isNaN(max) ? null : max;

    if (next < 0) return;
    if (safeMax !== null && next > safeMax) return;

    setQuantities((prev) => ({ ...prev, [key]: next }));
  };

  const totalAmount = useMemo(() => {
    return ticketCategories.reduce((sum, category, index) => {
      const key = String(category.id ?? category.name ?? index);
      const quantity = quantities[key] ?? 0;
      const price = typeof category.price === "number" ? category.price : Number(category.price);
      if (Number.isNaN(price)) return sum;
      return sum + quantity * price;
    }, 0);
  }, [ticketCategories, quantities]);

  const eventTitle = eventSnapshot?.name || "Sự kiện";
  const eventLocation = eventSnapshot?.location || "";
  const eventStartTime = formatDateTime(eventSnapshot?.startTime || "");
  const eventImage = eventSnapshot?.imageUrl || "";

  return (
    <main className="flex-1 bg-[#F8F9FA] text-gray-900 min-h-screen">
      <section className="flex flex-col items-center justify-center py-space-10 px-space-4">
        <div className="w-full max-w-[800px] flex flex-col gap-space-8">
          <header className="text-center mb-space-5">
            <div className="flex flex-col md:flex-row items-center justify-center gap-space-4 md:gap-space-6">
              {eventImage ? (
                <img
                  alt={eventTitle}
                  className="w-20 h-20 md:w-28 md:h-28 rounded-2xl object-cover border border-gray-200 shadow-md shrink-0"
                  src={eventImage}
                />
              ) : (
                <div className="w-20 h-20 md:w-28 md:h-28 rounded-2xl bg-gray-100 border border-gray-200 shadow-md shrink-0" />
              )}
              <div className="text-center md:text-left">
                <h1 className="font-heading-3 text-[32px] md:text-heading-3 text-gray-900 mb-space-3 tracking-tight" style={{ fontWeight: 300 }}>
                  {eventTitle}
                </h1>
                <div className="flex flex-col md:flex-row items-center md:items-center justify-center md:justify-start gap-space-4 text-gray-600 font-body-medium text-body-medium">
                  <span className="flex items-center gap-space-1">
                    <span className="material-symbols-outlined text-[20px]">calendar_today</span>
                    {eventStartTime || "Đang cập nhật"}
                  </span>
                  {eventLocation && (
                    <span className="flex items-center gap-space-1">
                      <span className="material-symbols-outlined text-[20px]">location_on</span>
                      {eventLocation}
                    </span>
                  )}
                </div>
              </div>
            </div>
          </header>

          <section className="flex flex-col gap-space-5">
            {ticketCategories.length === 0 ? (
              <div className="bg-white border border-gray-200 rounded shadow-sm p-space-6 text-center text-gray-600">
                Hiện chưa có hạng vé.
              </div>
            ) : (
              ticketCategories.map((category, index) => {
                const key = String(category.id ?? category.name ?? index);
                const quantity = quantities[key] ?? 0;
                const isFeatured = index === 2;
                const badgeText = index === 2 ? "Bán chạy" : "";

                return (
                  <div
                    key={key}
                    className={`relative border rounded shadow-sm p-space-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-space-5 transition-all duration-300 ${
                      isFeatured
                        ? "bg-gradient-to-br from-[#F0FDF4] to-white border-[#36F4A4] shadow-md"
                        : "bg-white border-gray-200 hover:border-gray-300 hover:shadow-md"
                    }`}
                  >
                    {badgeText && (
                      <div className="absolute -top-[1px] left-space-6 px-space-3 py-space-1 bg-[#36F4A4] text-black font-caption text-caption rounded-b-lg">
                        {badgeText}
                      </div>
                    )}
                    <div className={`flex-1 ${badgeText ? "mt-space-3 md:mt-0" : ""}`}>
                      <h3 className="font-heading-4 text-heading-4 text-gray-900 mb-space-2">
                        {category.name}
                      </h3>
                      <p className="text-gray-600 font-body-standard text-body-standard mb-space-3">
                        {category.description || "Vui long chon hang ve phu hop."}
                      </p>
                      <p className="font-body-large text-body-large text-emerald-700">
                        {formatCurrency(category.price)} VNĐ
                      </p>
                    </div>
                    <div className="flex items-center gap-space-4 bg-gray-50 rounded-full p-space-1 border border-gray-200">
                      <button
                        type="button"
                        aria-label="Giảm số lượng"
                        onClick={() => handleAdjust(category, -1)}
                        className="w-[40px] h-[40px] rounded-full flex items-center justify-center text-gray-600 hover:text-gray-900 hover:bg-gray-200 transition-colors focus:outline-none focus:ring-2 focus:ring-[#36F4A4]"
                      >
                        <span className="material-symbols-outlined">remove</span>
                      </button>
                      <span className="font-body-medium text-body-medium text-gray-900 w-[24px] text-center">
                        {quantity}
                      </span>
                      <button
                        type="button"
                        aria-label="Tăng số lượng"
                        onClick={() => handleAdjust(category, 1)}
                        className="w-[40px] h-[40px] rounded-full flex items-center justify-center text-gray-600 hover:text-gray-900 hover:bg-gray-200 transition-colors focus:outline-none focus:ring-2 focus:ring-[#36F4A4]"
                      >
                        <span className="material-symbols-outlined">add</span>
                      </button>
                    </div>
                  </div>
                );
              })
            )}
          </section>

          <div className="sticky bottom-space-6 mt-space-10 p-space-5 bg-white/90 backdrop-blur-xl border border-gray-200 rounded-lg shadow-[0_-4px_20px_rgba(0,0,0,0.05)] flex flex-col md:flex-row justify-between items-center gap-space-5 z-40">
            <div className="flex flex-col items-center md:items-start">
              <span className="font-caption text-caption text-gray-500 uppercase tracking-widest">Tổng cộng</span>
              <span className="font-heading-3 text-heading-3 text-gray-900">
                {formatCurrency(totalAmount)} VNĐ
              </span>
            </div>
            <button
              type="button"
              className="bg-[#36F4A4] text-black font-body-large text-body-large rounded-full px-[26px] py-[12px] flex items-center gap-space-2 hover:bg-[#28d68c] transition-colors focus:outline-none focus:ring-2 focus:ring-[#36F4A4] focus:ring-offset-2 focus:ring-offset-white w-full md:w-auto justify-center"
            >
              Tiếp tục thanh toán
              <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 0" }}>
                arrow_forward
              </span>
            </button>
          </div>
        </div>
      </section>
    </main>
  );
};

export default ChooseTicket;

