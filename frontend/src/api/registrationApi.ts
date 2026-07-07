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

  getById: (id: string) =>
    axiosInstance.get<ApiResponse<RegistrationResponse>>(`/registrations/${id}`),

  approve: (id: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/registrations/${id}/approve`),

  reject: (id: string, reason: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/registrations/${id}/reject`, { reason }),

  /** Hủy đăng ký - bắt buộc nhập lý do (đăng ký chuyển sang CANCELLED, không xóa). */
  cancel: (id: string, reason: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/registrations/${id}`, { data: { reason } }),

  /**
   * Ban tổ chức loại 1 đăng ký đã duyệt (trước/trong/sau khi race diễn ra). Nếu đăng ký đã có kết quả
   * "Đã công bố", cần confirmRevokePublish=true để xác nhận thu hồi công bố kết quả đó (xem lỗi 4).
   */
  disqualify: (id: string, reason: string, confirmRevokePublish?: boolean) =>
    axiosInstance.patch<ApiResponse<RegistrationResponse>>(`/registrations/${id}/disqualify`, {
      reason,
      confirmRevokePublish: confirmRevokePublish ? 'true' : 'false',
    }),
};