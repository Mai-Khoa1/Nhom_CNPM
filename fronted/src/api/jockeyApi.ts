import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { JockeyCreateRequest, JockeyUpdateRequest, JockeyResponse } from '@/types/jockey';

export const jockeyApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<JockeyResponse>>>('/jockeys', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<JockeyResponse>>(`/jockeys/${id}`),

  create: (data: JockeyCreateRequest) =>
    axiosInstance.post<ApiResponse<JockeyResponse>>('/jockeys', data),

  update: (id: number, data: JockeyUpdateRequest) =>
    axiosInstance.put<ApiResponse<JockeyResponse>>(`/jockeys/${id}`, data),

  delete: (id: number) =>
    axiosInstance.delete<ApiResponse<void>>(`/jockeys/${id}`),

  approve: (id: number) =>
    axiosInstance.patch<ApiResponse<JockeyResponse>>(`/jockeys/${id}/approve`),

  reject: (id: number, reason: string) =>
    axiosInstance.patch<ApiResponse<JockeyResponse>>(`/jockeys/${id}/reject`, { reason }),
};