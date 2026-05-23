import axios from 'axios';

const API_BASE_URL = 'https://d1cpe6xn6cl1ii.cloudfront.net/api/v1';
const FEATURED_EVENTS_ENDPOINT = "https://d1cpe6xn6cl1ii.cloudfront.net/api/v1/events/info-home";

const getAuthToken = () => localStorage.getItem('authToken') || '';

/**
 * Lấy Pre-signed URL từ Backend.
 * @param {File} file - File cần upload.
 * @returns {Promise<{uploadUrl: string, fileKey: string}>}
 */
export const getUploadUrl = (fileName, contentType) => {
  return axios.get(`${API_BASE_URL}/events/upload-url`, {
    params: {
      fileName,
      contentType,
    },
    headers: {
      "Bypass-Tunnel-Reminder": "true",
      'Authorization': `Bearer ${getAuthToken()}`,
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
  return axios.post(`${API_BASE_URL}/events/create`, eventData, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getAuthToken()}`,
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

export const getEventDetail = (eventId) => {
  const endpoint = `https://d1cpe6xn6cl1ii.cloudfront.net/api/v1/events/${eventId}/info-detail`;
  // if (!endpoint) {
  //   return Promise.resolve({ data: [] });
  // }
  return axios.get(endpoint, {
    headers: {
      "Bypass-Tunnel-Reminder": "true",
    },
  });
}

export const login = (payload) => {
  return axios.post(`${API_BASE_URL}/auth/login`, payload, {
    headers: {
      'Content-Type': 'application/json',
      "Bypass-Tunnel-Reminder": "true",
    },
  });
};

export const register = (payload) => {
  return axios.post(`${API_BASE_URL}/auth/register`, payload, {
    headers: {
      'Content-Type': 'application/json',
      "Bypass-Tunnel-Reminder": "true",
    },
  });
};

export const reserveOrder = (payload) => {
  const vwrAccessToken = localStorage.getItem('vwr_pass_token') || '';
  const authToken = getAuthToken();

  return axios.post(`${API_BASE_URL}/orders/reserve`, payload, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
      'x-vwr-pass': vwrAccessToken,
      "Bypass-Tunnel-Reminder": "true",
      "ngrok-skip-browser-warning": "true"
    },
  });
};
