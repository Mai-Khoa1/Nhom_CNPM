import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { LoginRequest, LoginResponse, RegisterRequest, RefreshTokenRequest, TokenRefreshResponse, UserResponse } from '@/types/auth';

export const authApi = {
  login: (data: LoginRequest) =>
    axiosInstance.post<ApiResponse<LoginResponse>>('/auth/login', data),

  register: (data: RegisterRequest) =>
    axiosInstance.post<ApiResponse<UserResponse>>('/auth/register', data),

  refreshToken: (data: RefreshTokenRequest) =>
    axiosInstance.post<ApiResponse<TokenRefreshResponse>>('/auth/refresh-token', data),

  logout: (refreshToken: string) =>
    axiosInstance.post<ApiResponse<void>>('/auth/logout', { refreshToken }),

  me: () =>
    axiosInstance.get<ApiResponse<UserResponse>>('/auth/me'),
};