import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { UpdateRequestResponse } from '@/types/updateRequest';

export const updateRequestApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<UpdateRequestResponse>>>('/update-requests', { params }),

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<UpdateRequestResponse>>(`/update-requests/${id}`),

  approve: (id: string) =>
    axiosInstance.patch<ApiResponse<UpdateRequestResponse>>(`/update-requests/${id}/approve`),

  reject: (id: string, reason: string) =>
    axiosInstance.patch<ApiResponse<UpdateRequestResponse>>(`/update-requests/${id}/reject`, { reason }),
};
