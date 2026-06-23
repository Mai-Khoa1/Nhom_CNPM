import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { HorseRankingResponse, JockeyRankingResponse } from '@/types/ranking';

export const rankingApi = {
  getHorseRankings: (params?: { seasonId?: number; page?: number; size?: number }) =>
    axiosInstance.get<ApiResponse<PageResponse<HorseRankingResponse>>>('/rankings/horses', { params }),

  getJockeyRankings: (params?: { seasonId?: number; page?: number; size?: number }) =>
    axiosInstance.get<ApiResponse<PageResponse<JockeyRankingResponse>>>('/rankings/jockeys', { params }),
};