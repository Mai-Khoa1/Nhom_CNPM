import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { RegistrationCreateRequest, RegistrationResponse } from '@/types/registration';

export const registrationApi = {
  create: (data: RegistrationCreateRequest) =>
    axiosInstance.post<ApiResponse<RegistrationResponse>>('/registrations', data),

  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<RegistrationResponse>>>('/registrations', { params }),

  getMy: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<RegistrationResponse>>>('/registrations/my', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<RegistrationResponse>>(`/registrations/${id}`),

  approve: (id: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/registrations/${id}/approve`),

  reject: (id: number, reason: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/registrations/${id}/reject`, { reason }),
};