import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { HorseRankingResponse, JockeyRankingResponse } from '@/types/ranking';

export const rankingApi = {
  getHorseRankings: (params?: { seasonId?: string; page?: number; size?: number }) =>
    axiosInstance.get<ApiResponse<PageResponse<HorseRankingResponse>>>('/rankings/horses', { params }),

  getJockeyRankings: (params?: { seasonId?: string; page?: number; size?: number }) =>
    axiosInstance.get<ApiResponse<PageResponse<JockeyRankingResponse>>>('/rankings/jockeys', { params }),
};