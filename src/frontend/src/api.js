import axios from 'axios';

const API_BASE_URL = '/api/v1'; // Giả định proxy đã được cấu hình trong vite.config.js
const FEATURED_EVENTS_ENDPOINT = "https://d1cpe6xn6cl1ii.cloudfront.net/api/v1/events/info-home";

//TODO: nho sua token
//const token = localStorage.getItem('authToken');
const tmpToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6IlVTRVIiLCJlbWFpbCI6InNwaWtlLnVzZXJAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InNwaWtlX3VzZXIiLCJpYXQiOjE3NzkxNTU2MDEsImV4cCI6MTc4MTc0NzYwMX0.Je2KmjTczKFVtUGBUjJwlwSSgyheUjdK_PNJZD4tz1vermmkdv4vLMK5cV2e52trBhzO0rhqm5FIwLKuUnYmX0y_rsX3CPS-Wj-41HJFHSNznXLBlS0KMZMy5X-x8PUZ-vbhQssEBibvZa-C3GXquEblJPH9oRT6XOtRDVtiNgDoqws0M0sLilaKVQSjF61Y8QCILS2gb3MPfb7lXwI7DNsibHU6OitaFqQg-c0d4dKVKtRTEBOb0YNmhdkKbc0O4Nmz9Pv4ZxdyQzHWMCkDBy1DK5TwU9RMTOyppV753tLAy4Eitj170GA1tiuqJt3UtgNoE156x0QViNaOFGfbGQ";
/**
 * Lấy Pre-signed URL từ Backend.
 * @param {File} file - File cần upload.
 * @returns {Promise<{uploadUrl: string, fileKey: string}>}
 */
export const getUploadUrl = (fileName, contentType) => {
  //TODO: nhớ sửa token
  //const token = localStorage.getItem('authToken');
  const token = tmpToken;
  return axios.get(`${API_BASE_URL}/events/upload-url`, {
    params: {
      fileName,
      contentType,
    },
    headers: {
      "Bypass-Tunnel-Reminder": "true",
      'Authorization': `Bearer ${token}`,
    }
  });
};

/**
 * Upload file lên S3 bằng Pre-signed URL.
 * @param {string} uploadUrl - Pre-signed URL nhận được từ backend.
 * @param {File} file - File cần upload.
 * @param {function} onUploadProgress - Callback để theo dõi tiến trình.
 * @returns {Promise<any>}
 */
export const uploadFileToS3 = (uploadUrl, file, onUploadProgress) => {
  return axios.put(uploadUrl, file, {
    headers: {
      'Content-Type': file.type,
    },
    onUploadProgress,
  });
};

/**
 * Gửi dữ liệu form tạo sự kiện về backend.
 * @param {object} eventData - Dữ liệu sự kiện.
 * @returns {Promise<any>}
 */
export const createEvent = (eventData) => {
  //TODO: nhớ đổi
//  const token = localStorage.getItem('authToken') ; // Or however you store your token
  const token = tmpToken;

  return axios.post(`${API_BASE_URL}/events/create`, eventData, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
      "Bypass-Tunnel-Reminder": "true",
    },
  });
};

/**
 * Lay danh sach su kien noi bat cho trang Home.
 * TODO: Cap nhat FEATURED_EVENTS_ENDPOINT theo backend.
 * @returns {Promise<{data: Array}>}
 */
export const getFeaturedEvents = () => {
  if (!FEATURED_EVENTS_ENDPOINT) {
    return Promise.resolve({ data: [] });
  }

  return axios.get(FEATURED_EVENTS_ENDPOINT, {
    headers: {
      "Bypass-Tunnel-Reminder": "true",
    },
  });
};
