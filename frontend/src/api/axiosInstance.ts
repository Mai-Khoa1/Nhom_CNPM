import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { tokenService } from '@/services/tokenService';
import { ENV } from '@/config/env';

export const axiosInstance = axios.create({
  baseURL: ENV.API_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 30000,
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value: string) => void;
  reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) prom.reject(error);
    else prom.resolve(token!);
  });
  failedQueue = [];
};

/**
 * Làm mới accessToken - dùng chung cho cả 2 trường hợp: thiếu accessToken khi gửi request,
 * và bị 401 sau khi gửi. Gộp chung để tránh gọi refresh-token song song (refresh token chỉ dùng 1 lần).
 */
const refreshAccessToken = async (refreshToken: string): Promise<string> => {
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject });
    });
  }

  isRefreshing = true;
  try {
    const { data } = await axios.post(`${ENV.API_URL}/auth/refresh-token`, { refreshToken });
    const { accessToken, refreshToken: newRefreshToken } = data.data;
    tokenService.saveTokens(accessToken, newRefreshToken);
    processQueue(null, accessToken);
    return accessToken;
  } catch (refreshError) {
    processQueue(refreshError, null);
    tokenService.clearTokens();
    window.location.href = '/login';
    throw refreshError;
  } finally {
    isRefreshing = false;
  }
};

axiosInstance.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  let token = tokenService.getAccessToken();
  // accessToken bị xóa/mất nhưng refreshToken còn -> chủ động lấy accessToken mới trước khi gửi request,
  // vì nhiều endpoint (vd /horses, /jockeys) permitAll cho khách xem công khai: thiếu token sẽ được server
  // coi là khách vãng lai (không lỗi 401) thay vì báo phiên hết hạn, nên interceptor response không có cơ hội refresh.
  if (!token) {
    const refreshToken = tokenService.getRefreshToken();
    if (refreshToken) {
      try {
        token = await refreshAccessToken(refreshToken);
      } catch {
        // refreshAccessToken đã tự điều hướng về /login khi refresh thất bại.
      }
    }
  }
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = tokenService.getRefreshToken();
      if (!refreshToken) {
        tokenService.clearTokens();
        window.location.href = '/login';
        return Promise.reject(error);
      }

      originalRequest._retry = true;
      try {
        const accessToken = await refreshAccessToken(refreshToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);