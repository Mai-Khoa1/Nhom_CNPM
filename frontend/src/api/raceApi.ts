import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { RaceCreateRequest, RaceUpdateRequest, RaceResponse } from '@/types/race';
import { RegistrationResponse } from '@/types/registration';
import { LaneResponse } from '@/types/lane';

export const raceApi = {
  getAll: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<RaceResponse>>>('/races', { params }),

  getById: (id: number) =>
    axiosInstance.get<ApiResponse<RaceResponse>>(`/races/${id}`),

  create: (data: RaceCreateRequest) =>
    axiosInstance.post<ApiResponse<RaceResponse>>('/races', data),

  update: (id: number, data: RaceUpdateRequest) =>
    axiosInstance.put<ApiResponse<RaceResponse>>(`/races/${id}`, data),

  delete: (id: number) =>
    axiosInstance.delete<ApiResponse<void>>(`/races/${id}`),

  publish: (id: number) =>
    axiosInstance.patch<ApiResponse<RaceResponse>>(`/races/${id}/publish`),

  getRegistrations: (id: number, params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<RegistrationResponse>>>(`/races/${id}/registrations`, { params }),

  getLanes: (id: number) =>
    axiosInstance.get<ApiResponse<LaneResponse[]>>(`/races/${id}/lanes`),
};