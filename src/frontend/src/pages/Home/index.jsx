import React, { useEffect, useMemo, useState } from "react";
import { getFeaturedEvents } from "../../api";

const formatEventDate = (isoString) => {
  if (!isoString) return "";
  const parsed = new Date(isoString);
  if (Number.isNaN(parsed.getTime())) return "";
  const day = String(parsed.getDate()).padStart(2, "0");
  const month = String(parsed.getMonth() + 1).padStart(2, "0");
  return `${day} thg ${month}`;
};

const formatPrice = (value) => {
  if (value === null || value === undefined) return "";
  return new Intl.NumberFormat("vi-VN").format(value);
};

const Home = () => {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [featuredEvents, setFeaturedEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [loadError, setLoadError] = useState("");

  const getEventId = (eventItem) => eventItem.eventId ?? eventItem.id;

  const goToDetail = (eventId) => {
    if (!eventId) return;
    window.location.hash = `/event/${eventId}`;
  };

  const slides = useMemo(() => {
    return featuredEvents.slice(0, 4).map((eventItem, index) => {
      const dateLabel = formatEventDate(eventItem.startTime);
      const locationLabel = eventItem.location || "";
      const dateLocation = [dateLabel, locationLabel].filter(Boolean).join(" • ");
      const eventId = getEventId(eventItem);

      return {
        id: eventId ?? `${eventItem.eventName}-${index}`,
        title: eventItem.eventName,
        dateLocation,
        imageUrl: eventItem.imageUrl,
      };
    });
  }, [featuredEvents]);

  useEffect(() => {
    if (slides.length === 0) {
      setCurrentSlide(0);
      return undefined;
    }

    const intervalId = window.setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 3000);

    return () => window.clearInterval(intervalId);
  }, [slides.length]);

  useEffect(() => {
    if (slides.length > 0 && currentSlide >= slides.length) {
      setCurrentSlide(0);
    }
  }, [currentSlide, slides.length]);

  useEffect(() => {
    let isMounted = true;

    const loadFeaturedEvents = async () => {
      try {
        setIsLoading(true);
        setLoadError("");
        const response = await getFeaturedEvents();

        if (!isMounted) return;
        setFeaturedEvents(response?.data ?? []);
      } catch (error) {
        if (!isMounted) return;
        setLoadError("Khong the tai danh sach su kien.");
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    loadFeaturedEvents();

    return () => {
      isMounted = false;
    };
  }, []);

  const handlePrevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length);
  };

  const handleNextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % slides.length);
  };

  return (
    <main className="bg-[#f8f9fa] text-[#191c1d]">
      <section className="relative min-h-[85vh] mb-theatrical-gap overflow-hidden">
        {slides.length === 0 ? (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="text-[#3b4a3f] text-[18px]">
              Chưa có sự kiện nổi bật.
            </div>
          </div>
        ) : (
          <>
            <div className="absolute inset-0">
              {slides.map((slide, index) => {
                const isActive = index === currentSlide;
                return (
                  <div
                    key={slide.id}
                    className={`absolute inset-0 transition-opacity duration-700 ease-in-out ${
                      isActive ? "opacity-100" : "opacity-0 invisible"
                    }`}
                  >
                    <img
                      alt={slide.title}
                      className="w-full h-full object-cover"
                      src={slide.imageUrl}
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-[#f8f9fa] via-[#f8f9fa]/60 to-transparent"></div>
                    <div className="absolute inset-0 flex flex-col justify-end pb-[120px] px-8 max-w-[1280px] mx-auto w-full">
                      <h1 className="text-[48px] leading-[56px] font-extrabold max-w-4xl">
                        {slide.title}
                      </h1>
                      <p className="text-[18px] leading-[28px] text-[#3b4a3f] max-w-2xl mt-4">
                        {slide.dateLocation}
                      </p>
                      <div className="pt-space-5">
                        <button
                          className="bg-[#00f59b] text-[#006b41] text-[16px] leading-[24px] rounded-full py-[12px] px-[32px] hover:bg-[#53ffab] transition-colors"
                          type="button"
                          onClick={() => goToDetail(slide.id)}
                        >
                          Mua vé ngay
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <div className="absolute inset-0 flex items-center justify-between px-8 pointer-events-none">
              <button
                className="pointer-events-auto w-12 h-12 rounded-full bg-[#f8f9fa]/40 backdrop-blur-md border border-[#b9cbbd]/30 flex items-center justify-center text-[#191c1d] hover:bg-[#f8f9fa]/60 transition-colors"
                id="hero-prev"
                type="button"
                onClick={handlePrevSlide}
              >
                <span className="material-symbols-outlined">chevron_left</span>
              </button>
              <button
                className="pointer-events-auto w-12 h-12 rounded-full bg-[#f8f9fa]/40 backdrop-blur-md border border-[#b9cbbd]/30 flex items-center justify-center text-[#191c1d] hover:bg-[#f8f9fa]/60 transition-colors"
                id="hero-next"
                type="button"
                onClick={handleNextSlide}
              >
                <span className="material-symbols-outlined">chevron_right</span>
              </button>
            </div>

            <div className="absolute bottom-10 left-1/2 -translate-x-1/2 flex gap-3 z-20">
              {slides.map((slide, index) => (
                <button
                  key={`${slide.id}-dot`}
                  className={`w-2.5 h-2.5 rounded-full transition-all cursor-pointer ${
                    index === currentSlide
                      ? "bg-[#191c1d]"
                      : "bg-[#191c1d]/30"
                  }`}
                  type="button"
                  onClick={() => setCurrentSlide(index)}
                />
              ))}
            </div>
          </>
        )}
      </section>

      <section className="max-w-[1280px] mx-auto px-8 mb-theatrical-gap">
        <div className="flex justify-between items-end mb-space-9">
          <h2 className="text-[32px] leading-[40px] font-bold">Sự kiện nổi bật</h2>
          <a
            className="text-[16px] leading-[24px] text-[#006d42] hover:text-[#00e38f] transition-colors flex items-center gap-1"
            href="#"
          >
            Xem tất cả
            <span
              className="material-symbols-outlined"
              style={{ fontVariationSettings: "'FILL' 0" }}
            >
              arrow_forward
            </span>
          </a>
        </div>

        {isLoading ? (
          <div className="text-[#3b4a3f]">Đang tải sự kiện...</div>
        ) : loadError ? (
          <div className="text-[#93000a]">{loadError}</div>
        ) : featuredEvents.length === 0 ? (
          <div className="text-[#3b4a3f]">
            Chưa có sự kiện nổi bật, hãy cập nhật API route.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-space-5">
            {featuredEvents.map((eventItem, index) => (
              <div
                key={`${eventItem.eventName}-${index}`}
                className="bg-[#f8f9fa] border border-[#b9cbbd] rounded-xl overflow-hidden multi-shadow glass-glow group cursor-pointer hover:bg-[#f3f4f5] transition-all"
                onClick={() => goToDetail(getEventId(eventItem))}
              >
                <div className="h-64 bg-[#edeeef] relative overflow-hidden">
                  <img
                    alt={eventItem.eventName}
                    className="w-full h-full object-cover opacity-90 group-hover:opacity-100 transition-opacity duration-500"
                    src={eventItem.imageUrl}
                  />
                </div>
                <div className="p-space-5 space-y-space-3 relative">
                  <h3 className="text-[24px] leading-[32px] font-bold text-[#191c1d]">
                    {eventItem.eventName}
                  </h3>
                  <p className="text-[16px] leading-[24px] text-[#3b4a3f]">
                    {eventItem.location}
                  </p>
                  <div className="py-2">
                    <span className="text-[16px] leading-[24px] font-semibold text-[#006d42]">
                      Từ {formatPrice(eventItem.minPrice)} VNĐ
                    </span>
                  </div>
                  <div className="flex justify-between items-center pt-space-2 border-t border-[#b9cbbd]/50">
                    <span className="text-[12px] leading-[16px] font-bold text-[#3b4a3f]">
                      {formatEventDate(eventItem.startTime)} • {eventItem.location}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
};

export default Home;

