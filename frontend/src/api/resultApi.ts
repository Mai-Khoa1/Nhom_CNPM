import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { ResultEntryRequest, ResultResponse } from '@/types/result';

export const resultApi = {
  create: (data: ResultEntryRequest) =>
    axiosInstance.post<ApiResponse<ResultResponse>>('/results', data),

  getByRaceId: (raceId: number) =>
    axiosInstance.get<ApiResponse<ResultResponse>>(`/results/${raceId}`),

  update: (raceId: number, data: ResultEntryRequest) =>
    axiosInstance.put<ApiResponse<ResultResponse>>(`/results/${raceId}`, data),

  publish: (raceId: number) =>
    axiosInstance.patch<ApiResponse<void>>(`/results/${raceId}/publish`),
};