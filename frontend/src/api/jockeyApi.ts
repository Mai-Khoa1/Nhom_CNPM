import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { JockeyCreateRequest, JockeyUpdateRequest, JockeyResponse, JockeyListParams } from '@/types/jockey';

export const jockeyApi = {
  getAll: (params?: JockeyListParams) =>
    axiosInstance.get<ApiResponse<PageResponse<JockeyResponse>>>('/jockeys', { params }),

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<JockeyResponse>>(`/jockeys/${id}`),

  create: (data: JockeyCreateRequest) =>
    axiosInstance.post<ApiResponse<JockeyResponse>>('/jockeys', data),

  update: (id: string, data: JockeyUpdateRequest) =>
    axiosInstance.put<ApiResponse<JockeyResponse>>(`/jockeys/${id}`, data),

  delete: (id: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/jockeys/${id}`),
};