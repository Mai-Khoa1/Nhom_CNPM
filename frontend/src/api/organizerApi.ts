import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { OrganizerResponse } from '@/types/organizer';

export const organizerApi = {
  getAll: () =>
    axiosInstance.get<ApiResponse<OrganizerResponse[]>>('/organizers'),
};
