import axios from 'axios';

const API_BASE_URL = '/api/v1'; // Giả định proxy đã được cấu hình trong vite.config.js

//TODO: nho sua token
//const token = localStorage.getItem('authToken');
const tmpToken = 'eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzIiwicm9sZSI6IlVTRVIiLCJlbWFpbCI6InNwaWtlLnVzZXJAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InNwaWtlX3VzZXIiLCJpYXQiOjE3Nzg4NTk2MTMsImV4cCI6MTc4MTQ1MTYxM30.h8pzRXXna5htUBo68iUlmWP-sRmgrC_364TY42ew4LW-2TG5cvVgn5CNBH861NFvXIZwjH_seO-SIpj3iyiBPFluscLNQNuja_Lr2QY7V9klzhIC8Ab-gWjBK0ajDCQGJQ5GryagRnoBUdFnrauVyEkk495lSs27cF5_S4woSQK85xgligPgdgp6K-k1uMvuRqyUqtCmV7SE1xO-Dv125O4MZLRYARxnoSRMEe0NW3PVlnspCbG0H0v8hW5XEAKODkAKX69PiXJ49A2ZzlBS_he3WpoXYOpwFLpB3r-hWNhrrUiFnORHU1LKFqrWIkFgHWqiDX3HaBOxu9WX8B_WKg'

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
      Authorization: `Bearer ${token}`
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
  const formData = new FormData();
  //TODO: nhớ đổi
//  const token = localStorage.getItem('authToken') ; // Or however you store your token
  const token = tmpToken;

  Object.keys(eventData).forEach(key => {
    if (key === 'listOfImageUrls') {
      eventData[key].forEach(url => {
        formData.append(key, url);
      });
    } else {
      formData.append(key, eventData[key]);
    }
  });

  return axios.post(`${API_BASE_URL}/events/create`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}` // Add the token to the header
    },
  });
};
