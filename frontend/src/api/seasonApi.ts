import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { SeasonCreateRequest, SeasonUpdateRequest, SeasonResponse, PointRuleResponse, PointRuleRequest } from '@/types/season';

export const seasonApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<SeasonResponse>>>('/seasons', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<SeasonResponse>>(`/seasons/${id}`),

  create: (data: SeasonCreateRequest) =>
    axiosInstance.post<ApiResponse<SeasonResponse>>('/seasons', data),

  update: (id: number, data: SeasonUpdateRequest) =>
    axiosInstance.put<ApiResponse<SeasonResponse>>(`/seasons/${id}`, data),

  close: (id: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/seasons/${id}/close`),

  getPointRules: (id: number) =>
    axiosInstance.get<ApiResponse<PointRuleResponse[]>>(`/seasons/${id}/point-rules`),

  updatePointRules: (id: number, rules: PointRuleRequest[]) =>
    axiosInstance.put<ApiResponse<PointRuleResponse[]>>(`/seasons/${id}/point-rules`, rules),
};