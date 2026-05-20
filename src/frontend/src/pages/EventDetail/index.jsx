import React, { useEffect, useMemo, useState } from "react";
import { getEventDetail } from "../../api";

const formatDateTime = (isoString) => {
  if (!isoString) return "";
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return "";

  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
};

const formatPrice = (value) => {
  if (value === null || value === undefined) return "";
  return new Intl.NumberFormat("vi-VN").format(value);
};

const EventDetail = ({ eventId }) => {
  const [eventDetail, setEventDetail] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    if (!eventId) return;

    let isMounted = true;

    const fetchDetail = async () => {
      try {
        setIsLoading(true);
        setLoadError("");
        const response = await getEventDetail(eventId);
        if (!isMounted) return;
        setEventDetail(response?.data ?? null);
      } catch (error) {
        if (!isMounted) return;
        setLoadError("Khong the tai chi tiet su kien.");
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    fetchDetail();

    return () => {
      isMounted = false;
    };
  }, [eventId]);

  const galleryImages = useMemo(() => {
    if (!eventDetail?.imageUrls?.length) return [];
    return eventDetail.imageUrls.slice(0, 4);
  }, [eventDetail]);

  const ticketCategories = useMemo(() => {
    if (!eventDetail?.categoryItemList?.length) return [];
    return eventDetail.categoryItemList;
  }, [eventDetail]);

  const minPrice = useMemo(() => {
    if (!ticketCategories.length) return null;
    return ticketCategories.reduce((min, item) => {
      const price = typeof item.price === "number" ? item.price : Number(item.price);
      if (Number.isNaN(price)) return min;
      return min === null ? price : Math.min(min, price);
    }, null);
  }, [ticketCategories]);

  const heroImage = galleryImages[0] || eventDetail?.imageUrls?.[0] || "";

  if (!eventId) {
    return (
      <main className="bg-[#f8f9fa] text-[#191c1d] min-h-screen flex items-center justify-center">
        <p className="text-[#3b4a3f]">Khong tim thay su kien.</p>
      </main>
    );
  }

  if (isLoading) {
    return (
      <main className="bg-[#f8f9fa] text-[#191c1d] min-h-screen flex items-center justify-center">
        <p className="text-[#3b4a3f]">Đang tải chi tiết sự kiện...</p>
      </main>
    );
  }

  if (loadError) {
    return (
      <main className="bg-[#f8f9fa] text-[#191c1d] min-h-screen flex items-center justify-center">
        <p className="text-[#93000a]">{loadError}</p>
      </main>
    );
  }

  if (!eventDetail) {
    return null;
  }

  return (
    <main className="bg-[#f8f9fa] text-[#191c1d]">
      <header
        className="relative w-full h-[716px] min-h-[600px] flex items-end pb-space-10 pt-32"
        style={{
          backgroundImage: heroImage
            ? `linear-gradient(to top, #F8F9FA 0%, transparent 100%), url('${heroImage}')`
            : "linear-gradient(to top, #F8F9FA 0%, transparent 100%)",
          backgroundSize: "cover",
          backgroundPosition: "center",
        }}
      >
        <div className="w-full max-w-[1280px] mx-auto px-12 relative z-10">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/80 border border-gray-200 backdrop-blur-md mb-space-5 shadow-sm">
            <span className="w-2 h-2 rounded-full bg-neon-green shadow-[0_0_10px_#36F4A4]"></span>
            <span className="font-overline text-overline text-[#005f3b] font-semibold tracking-widest uppercase">
              Đang mở bán
            </span>
          </div>
          <h1 className="font-heading-1 text-heading-1 text-gray-900 max-w-4xl tracking-tight font-bold">
            {eventDetail.name}
          </h1>
        </div>
      </header>

      <section className="w-full max-w-[1280px] mx-auto px-12 py-theatrical-gap grid grid-cols-1 lg:grid-cols-12 gap-space-10">
        <div className="lg:col-span-8 flex flex-col gap-space-10">
          <div className="flex flex-wrap gap-space-8 py-space-6 border-y border-gray-200">
            <div className="flex items-start gap-space-4">
              <span
                className="material-symbols-outlined text-neon-green text-3xl"
                style={{ fontVariationSettings: "'FILL' 1" }}
              >
                calendar_month
              </span>
              <div>
                <p className="font-caption text-caption text-gray-500 mb-1 uppercase tracking-wider">
                  Thời gian bắt đầu
                </p>
                <p className="font-body-large text-body-large text-gray-900">
                  {formatDateTime(eventDetail.startTime)}
                </p>
              </div>
            </div>
            <div className="w-px h-12 bg-gray-200 hidden md:block"></div>
            <div className="flex items-start gap-space-4">
              <span className="material-symbols-outlined text-gray-400 text-3xl">
                schedule
              </span>
              <div>
                <p className="font-caption text-caption text-gray-500 mb-1 uppercase tracking-wider">
                  Thời gian kết thúc
                </p>
                <p className="font-body-large text-body-large text-gray-900">
                  {formatDateTime(eventDetail.endTime)}
                </p>
              </div>
            </div>
          </div>

          <div className="flex items-start gap-space-4">
            <div className="w-12 h-12 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center shrink-0">
              <span className="material-symbols-outlined text-neon-green">location_on</span>
            </div>
            <div>
              <h3 className="font-heading-4 text-heading-4 text-gray-900 mb-2 font-semibold">
                {eventDetail.location}
              </h3>
              <p className="font-body-medium text-body-medium text-gray-600 max-w-xl">
                {eventDetail.location}
              </p>
            </div>
          </div>

          <article className="prose prose-lg max-w-none">
            <h3 className="font-heading-4 text-heading-4 text-gray-900 mb-space-5 font-semibold">
              Về sự kiện này
            </h3>
            <div className="font-body-standard text-body-standard text-gray-700 space-y-6 leading-relaxed">
              <p>{eventDetail.description}</p>
            </div>
          </article>
        </div>

        <div className="lg:col-span-4">
          <div className="sticky top-32 bg-white border border-gray-200 rounded-DEFAULT p-space-6 shadow-md flex flex-col gap-space-6 relative overflow-hidden group">
            <div className="absolute -top-24 -right-24 w-64 h-64 bg-neon-green/10 rounded-full blur-[80px] pointer-events-none group-hover:bg-neon-green/20 transition-all duration-700"></div>
            <div className="relative z-10">
              <p className="font-overline text-overline text-gray-500 tracking-widest uppercase mb-2">
                Giá vé từ
              </p>
              <div className="flex items-baseline gap-2 mb-space-6">
                {minPrice === null ? (
                  <span className="4 text-heading-4 text-gray-900 font-semibold">
                    Đang cập nhật
                  </span>
                ) : (
                  <>
                    <span className="font-heading-4 text-heading-4 text-gray-900 font-semibold">
                      {formatPrice(minPrice)}
                    </span>
                    <span className="font-body-medium text-body-medium text-gray-500">VNĐ</span>
                  </>
                )}
              </div>

              {ticketCategories.length > 0 && (
                <div className="space-y-space-4 mb-space-8">
                  {ticketCategories.map((category, index) => (
                    <div
                      key={`${category.name}-${index}`}
                      className={`flex justify-between items-center py-3 ${
                        index < ticketCategories.length - 1 ? "border-b border-gray-100" : ""
                      }`}
                    >
                      <div className="flex flex-col gap-1">
                        <span className="font-body-medium text-body-medium text-gray-700">
                          {category.name}
                        </span>
                        {category.description && (
                          <span className="text-xs text-gray-500">{category.description}</span>
                        )}
                      </div>
                      <span className="font-body-medium text-body-medium text-gray-500">
                        {formatPrice(category.price)}
                      </span>
                    </div>
                  ))}
                </div>
              )}

              <button
                className="w-full bg-[#36F4A4] text-gray-900 font-body-large text-body-large font-semibold rounded-full py-4 px-6 hover:bg-[#2bd990] hover:shadow-lg focus:ring-2 focus:ring-[#36F4A4] focus:ring-offset-2 focus:ring-offset-white transition-all flex items-center justify-center gap-2"
                type="button"
              >
                <span>Mua vé ngay</span>
                <span className="material-symbols-outlined text-xl">arrow_forward</span>
              </button>

            </div>
          </div>
        </div>
      </section>

      <section className="w-full max-w-[1280px] mx-auto px-12 py-theatrical-gap border-t border-gray-200">
        <h2 className="font-heading-3 text-heading-3 text-gray-900 mb-space-10 font-semibold">
          Không gian sự kiện
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-4 grid-rows-2 gap-space-4 h-[600px]">
          {galleryImages.map((imageUrl, index) => {
            const isLarge = index === 0;
            const classes = isLarge
              ? "md:col-span-2 md:row-span-2"
              : index === 3
              ? "md:col-span-2 md:row-span-1"
              : "md:col-span-1 md:row-span-1";

            return (
              <div
                key={`${imageUrl}-${index}`}
                className={`${classes} rounded-DEFAULT overflow-hidden relative group`}
              >
                <div className="absolute inset-0 bg-black/10 group-hover:bg-transparent transition-colors z-10"></div>
                <img
                  alt={`Gallery image ${index + 1}`}
                  className="w-full h-full object-cover transform group-hover:scale-105 transition-transform duration-700"
                  src={imageUrl}
                />
              </div>
            );
          })}
        </div>
      </section>

      {/*<footer className="w-full border-t border-gray-200 bg-white">*/}
      {/*  <div className="flex flex-col md:flex-row justify-between items-center py-12 px-12 max-w-[1280px] mx-auto gap-8">*/}
      {/*    <p className="font-['Plus_Jakarta_Sans'] text-sm text-gray-500">*/}
      {/*      © 2026 Spike Ticket. All rights reserved.*/}
      {/*    </p>*/}
      {/*    <div className="flex gap-space-6">*/}
      {/*      <a className="font-['Plus_Jakarta_Sans'] text-sm text-gray-500 hover:text-gray-900 transition-all" href="#">*/}
      {/*        Chinh sach bao mat*/}
      {/*      </a>*/}
      {/*      <a className="font-['Plus_Jakarta_Sans'] text-sm text-gray-500 hover:text-gray-900 transition-all" href="#">*/}
      {/*        Dieu khoan su dung*/}
      {/*      </a>*/}
      {/*      <a className="font-['Plus_Jakarta_Sans'] text-sm text-gray-500 hover:text-gray-900 transition-all" href="#">*/}
      {/*        Lien he*/}
      {/*      </a>*/}
      {/*      <a className="font-['Plus_Jakarta_Sans'] text-sm text-gray-500 hover:text-gray-900 transition-all" href="#">*/}
      {/*        Trung tam ho tro*/}
      {/*      </a>*/}
      {/*    </div>*/}
      {/*  </div>*/}
      {/*</footer>*/}
    </main>
  );
};

export default EventDetail;

