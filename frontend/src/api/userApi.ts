import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { UserCreateRequest, UserUpdateRequest, UserResponse } from '@/types/user';
import { Role } from '@/types/enums';

export const userApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<UserResponse>>>('/users', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<UserResponse>>(`/users/${id}`),

  create: (data: UserCreateRequest) =>
    axiosInstance.post<ApiResponse<UserResponse>>('/users', data),

  update: (id: number, data: UserUpdateRequest) =>
    axiosInstance.put<ApiResponse<UserResponse>>(`/users/${id}`, data),

  delete: (id: number) =>
    axiosInstance.delete<ApiResponse<void>>(`/users/${id}`),

  lock: (id: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/users/${id}/lock`),

  unlock: (id: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/users/${id}/unlock`),

  changeRole: (id: number, role: Role) =>
    axiosInstance.patch<ApiResponse<UserResponse>>(`/users/${id}/role`, { role }),
};