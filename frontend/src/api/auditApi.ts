import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { AuditLogResponse } from '@/types/audit';

export const auditApi = {
  getAll: (params?: { action?: string }) => {
    if (params?.action) {
      return axiosInstance.get<ApiResponse<AuditLogResponse[]>>(
        `/nhat-ky-hoat-dong/action/${params.action}`
      );
    }
    return axiosInstance.get<ApiResponse<AuditLogResponse[]>>('/nhat-ky-hoat-dong');
  },
};
