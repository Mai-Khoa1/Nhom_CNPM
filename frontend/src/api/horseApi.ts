import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { HorseCreateRequest, HorseUpdateRequest, HorseResponse, HorseListParams } from '@/types/horse';
import { RegistrationResponse } from '@/types/registration';

export const horseApi = {
  getAll: (params: HorseListParams) =>
    axiosInstance.get<ApiResponse<PageResponse<HorseResponse>>>('/horses', { params }),

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<HorseResponse>>(`/horses/${id}`),

  create: (data: HorseCreateRequest) =>
    axiosInstance.post<ApiResponse<HorseResponse>>('/horses', data),

  update: (id: string, data: HorseUpdateRequest) =>
    axiosInstance.put<ApiResponse<HorseResponse>>(`/horses/${id}`, data),

  delete: (id: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/horses/${id}`),

  getRaceHistory: (id: string) =>
    axiosInstance.get<ApiResponse<RegistrationResponse[]>>(`/horses/${id}/race-history`),

  getHealth: (id: string) =>
    axiosInstance.get<ApiResponse<unknown[]>>(`/horses/${id}/health`),

  getDoping: (id: string) =>
    axiosInstance.get<ApiResponse<unknown[]>>(`/horses/${id}/doping`),
};