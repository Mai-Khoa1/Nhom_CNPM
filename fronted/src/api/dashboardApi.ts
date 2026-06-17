import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { DashboardStatsResponse } from '@/types/dashboard';

export const dashboardApi = {
  getStats: () =>
    axiosInstance.get<ApiResponse<DashboardStatsResponse>>('/dashboard/stats'),

  getUpcoming: () =>
    axiosInstance.get<ApiResponse<unknown[]>>('/dashboard/upcoming'),

  getRecent: () =>
    axiosInstance.get<ApiResponse<unknown[]>>('/dashboard/recent'),
};