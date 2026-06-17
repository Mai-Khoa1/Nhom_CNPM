import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { HorseCreateRequest, HorseUpdateRequest, HorseResponse, HorseListParams } from '@/types/horse';
import { RegistrationResponse } from '@/types/registration';

export const horseApi = {
  getAll: (params: HorseListParams) =>
    axiosInstance.get<ApiResponse<PageResponse<HorseResponse>>>('/horses', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<HorseResponse>>(`/horses/${id}`),

  create: (data: HorseCreateRequest) =>
    axiosInstance.post<ApiResponse<HorseResponse>>('/horses', data),

  update: (id: number, data: HorseUpdateRequest) =>
    axiosInstance.put<ApiResponse<HorseResponse>>(`/horses/${id}`, data),

  delete: (id: number) =>
    axiosInstance.delete<ApiResponse<void>>(`/horses/${id}`),

  approve: (id: number) =>
    axiosInstance.patch<ApiResponse<HorseResponse>>(`/horses/${id}/approve`),

  reject: (id: number, reason: string) =>
    axiosInstance.patch<ApiResponse<HorseResponse>>(`/horses/${id}/reject`, { reason }),

  disqualify: (id: number, reason: string) =>
    axiosInstance.patch<ApiResponse<HorseResponse>>(`/horses/${id}/disqualify`, { reason }),

  getRaceHistory: (id: number) =>
    axiosInstance.get<ApiResponse<RegistrationResponse[]>>(`/horses/${id}/race-history`),

  getHealth: (id: number) =>
    axiosInstance.get<ApiResponse<unknown[]>>(`/horses/${id}/health`),

  getDoping: (id: number) =>
    axiosInstance.get<ApiResponse<unknown[]>>(`/horses/${id}/doping`),
};