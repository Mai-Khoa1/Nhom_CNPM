import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { UserCreateRequest, UserUpdateRequest, UserResponse } from '@/types/user';
import { Role } from '@/types/enums';

export const userApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<UserResponse>>>('/users', { params }),

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<UserResponse>>(`/users/${id}`),

  create: (data: UserCreateRequest) =>
    axiosInstance.post<ApiResponse<UserResponse>>('/users', data),

  update: (id: string, data: UserUpdateRequest) =>
    axiosInstance.put<ApiResponse<UserResponse>>(`/users/${id}`, data),

  delete: (id: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/users/${id}`),

  lock: (id: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/users/${id}/lock`),

  unlock: (id: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/users/${id}/unlock`),

  changeRole: (id: string, role: Role) =>
    axiosInstance.patch<ApiResponse<UserResponse>>(`/users/${id}/role`, { role }),
};