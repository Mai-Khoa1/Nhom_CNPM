import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { AuditLogResponse } from '@/types/audit';
import { AuditAction } from '@/types/enums';

export const auditApi = {
  getAll: (params?: {
    page?: number;
    size?: number;
    userId?: string;
    action?: AuditAction;
    targetType?: string;
    fromDate?: string;
    toDate?: string;
  }) => axiosInstance.get<ApiResponse<PageResponse<AuditLogResponse>>>('/audit-logs', { params }),
};