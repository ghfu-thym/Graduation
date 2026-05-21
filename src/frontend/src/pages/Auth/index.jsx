import React, { useEffect, useMemo, useState } from "react";
import { login as apiLogin, register as apiRegister } from "../../api";

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const AuthPage = ({ initialMode = "login" }) => {
  const [mode, setMode] = useState(initialMode === "register" ? "register" : "login");
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
  });
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const isRegister = mode === "register";

  useEffect(() => {
    setMode(initialMode === "register" ? "register" : "login");
  }, [initialMode]);

  useEffect(() => {
    setFormData({ username: "", password: "", email: "" });
    setErrors({});
    setSubmitError("");
  }, [mode]);

  const title = useMemo(() => (isRegister ? "Tạo tài khoản" : "Đăng nhập"), [isRegister]);
  const subtitle = useMemo(
    () =>
      isRegister
        ? "Tạo tài khoản để mua vé"
        : "Chào mừng trở lại, hãy đăng nhập để tiếp tục",
    [isRegister]
  );

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    const nextErrors = {};

    if (!formData.username.trim()) {
      nextErrors.username = "Vui lòng nhập tên đăng nhập";
    } else if (formData.username.length > 30) {
      nextErrors.username = "Tên đăng nhập tối đa 30 ký tự.";
    }

    if (!formData.password.trim()) {
      nextErrors.password = "Vui lòng nập mật kẩu";
    } else if (isRegister && (formData.password.length < 6 || formData.password.length > 32)) {
      nextErrors.password = "Mật khẩu cần 6-32 ký tự.";
    }

    if (isRegister) {
      if (!formData.email.trim()) {
        nextErrors.email = "Vui long nhap email.";
      } else if (!emailRegex.test(formData.email)) {
        nextErrors.email = "Email khong hop le.";
      } else if (formData.email.length > 50) {
        nextErrors.email = "Email toi da 50 ky tu.";
      }
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const isLikelyJwt = (value) => {
    if (typeof value !== "string") return false;
    const parts = value.split(".");
    return parts.length === 3 && parts.every((part) => part.length > 0);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitError("");

    if (!validate()) return;

    try {
      setIsSubmitting(true);
      if (isRegister) {
        const response = await apiRegister({
          username: formData.username.trim(),
          password: formData.password,
          email: formData.email.trim(),
        });
        const token = typeof response?.data === "string" ? response.data : "";
        if (isLikelyJwt(token)) {
          localStorage.setItem("authToken", token);
          localStorage.setItem("authUsername", formData.username.trim());
          const redirectTo = localStorage.getItem("postLoginRedirect") || "#/home";
          localStorage.removeItem("postLoginRedirect");
          window.location.hash = redirectTo;
        } else {
          setSubmitError("Token khong hop le. Kiem tra endpoint auth dang tro toi backend.");
        }
      } else {
        const response = await apiLogin({
          username: formData.username.trim(),
          password: formData.password,
        });
        const token = typeof response?.data === "string" ? response.data : "";
        if (isLikelyJwt(token)) {
          localStorage.setItem("authToken", token);
          localStorage.setItem("authUsername", formData.username.trim());
          const redirectTo = localStorage.getItem("postLoginRedirect") || "#/home";
          localStorage.removeItem("postLoginRedirect");
          window.location.hash = redirectTo;
        } else {
          setSubmitError("Token khong hop le. Kiem tra endpoint auth dang tro toi backend.");
        }
      }
    } catch (error) {
      const message = error?.response?.data?.message || "Khong the thuc hien yeu cau. Vui long thu lai.";
      setSubmitError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="flex-1 bg-gray-50 text-gray-900">
      <section className="min-h-[calc(100vh-80px)] flex items-center justify-center px-space-6 py-space-10">
        <div className="w-full max-w-4xl grid grid-cols-1 lg:grid-cols-2 gap-space-10">
          <div className="rounded-DEFAULT border border-gray-200 bg-white px-space-8 py-space-10 shadow-md">
            <div className="inline-flex items-center gap-2 rounded-full border border-gray-200 px-space-4 py-space-2 mb-space-6">
              <span className="w-2 h-2 rounded-full bg-neon-green"></span>
              <span className="font-overline text-overline text-gray-500 tracking-widest uppercase">Spike Ticket</span>
            </div>
            <h1 className="font-heading-2 text-heading-2 text-gray-900 mb-space-4 font-light">
              {title}
            </h1>
            <p className="font-body-standard text-body-standard text-gray-500 max-w-md">
              {subtitle}
            </p>

            <div className="mt-space-8 flex items-center gap-space-3">
              <button
                type="button"
                onClick={() => setMode("login")}
                className={`rounded-full px-space-5 py-2 text-sm font-medium border transition-colors ${
                  !isRegister
                    ? "bg-gray-900 text-white border-transparent"
                    : "bg-transparent text-gray-700 border-gray-200 hover:border-gray-400"
                }`}
              >
                Đăng nhập
              </button>
              <button
                type="button"
                onClick={() => setMode("register")}
                className={`rounded-full px-space-5 py-2 text-sm font-medium border transition-colors ${
                  isRegister
                    ? "bg-gray-900 text-white border-transparent"
                    : "bg-transparent text-gray-700 border-gray-200 hover:border-gray-400"
                }`}
              >
                Đăng ký
              </button>
            </div>
          </div>

          <div className="rounded-DEFAULT border border-gray-200 bg-white px-space-8 py-space-10 shadow-md">
            <form onSubmit={handleSubmit} className="space-y-space-6">
              <div>
                <label htmlFor="username" className="block text-sm text-gray-600 mb-space-2">
                  Tên đăng nhập
                </label>
                <input
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  placeholder="Nhập tên đăng nhập"
                  className="w-full rounded-lg border border-gray-300 bg-white px-space-4 py-space-3 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-neon-green"
                />
                {errors.username && <p className="mt-2 text-xs text-red-500">{errors.username}</p>}
              </div>

              {isRegister && (
                <div>
                  <label htmlFor="email" className="block text-sm text-gray-600 mb-space-2">
                    Email
                  </label>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="you@email.com"
                    className="w-full rounded-lg border border-gray-300 bg-white px-space-4 py-space-3 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-neon-green"
                  />
                  {errors.email && <p className="mt-2 text-xs text-red-500">{errors.email}</p>}
                </div>
              )}

              <div>
                <label htmlFor="password" className="block text-sm text-gray-600 mb-space-2">
                  Mật khẩu
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleChange}
                  placeholder="Nhập mật khẩu"
                  className="w-full rounded-lg border border-gray-300 bg-white px-space-4 py-space-3 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-neon-green"
                />
                {errors.password && <p className="mt-2 text-xs text-red-500">{errors.password}</p>}
              </div>

              {submitError && (
                <div className="rounded-lg border border-red-200 bg-red-50 px-space-4 py-space-3 text-xs text-red-600">
                  {submitError}
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting}
                className="w-full rounded-full bg-gray-900 text-white font-medium py-3 transition-all hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-neon-green disabled:opacity-60"
              >
                {isSubmitting ? "Đang xử lý..." : isRegister ? "Tạo tài khoản" : "Đăng nhập"}
              </button>

              <p className="text-xs text-gray-500">
                {isRegister
                  ? "Đã có tài khoản? Chọn Đăng nhập để tiếp tục."
                  : "Chưa có tài khoản? Chọn Đăng ký để tạo mới."}
              </p>
            </form>
          </div>
        </div>
      </section>
    </main>
  );
};

export default AuthPage;

