import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { NotificationResponse } from '@/types/notification';

export const notificationApi = {
  getAll: (params?: { page?: number; size?: number; isRead?: boolean }) =>
    axiosInstance.get<ApiResponse<PageResponse<NotificationResponse>>>('/notifications', { params }),

  markRead: (id: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/notifications/${id}/read`),

  markAllRead: () =>
    axiosInstance.patch<ApiResponse<void>>('/notifications/read-all'),

  getUnreadCount: () =>
    axiosInstance.get<ApiResponse<{ count: number }>>('/notifications/unread-count'),
};