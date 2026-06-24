import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { LaneAssignRequest, LaneResponse } from '@/types/lane';

export const laneApi = {
  assign: (data: LaneAssignRequest) =>
    axiosInstance.post<ApiResponse<LaneResponse>>('/lanes', data),

  update: (id: string, data: LaneAssignRequest) =>
    axiosInstance.put<ApiResponse<LaneResponse>>(`/lanes/${id}`, data),

  delete: (id: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/lanes/${id}`),
};