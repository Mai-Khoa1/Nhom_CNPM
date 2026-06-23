import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { ResultEntryRequest, ResultResponse } from '@/types/result';

export const resultApi = {
  create: (data: ResultEntryRequest) =>
    axiosInstance.post<ApiResponse<ResultResponse>>('/results', data),

  getByRaceId: (raceId: string) =>
    axiosInstance.get<ApiResponse<ResultResponse>>(`/results/${raceId}`),

  update: (raceId: string, data: ResultEntryRequest) =>
    axiosInstance.put<ApiResponse<ResultResponse>>(`/results/${raceId}`, data),

  publish: (raceId: string) =>
    axiosInstance.patch<ApiResponse<void>>(`/results/${raceId}/publish`),
};