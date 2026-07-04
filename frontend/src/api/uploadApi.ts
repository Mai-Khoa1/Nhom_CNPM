import { axiosInstance } from './axiosInstance';
import { ApiResponse } from '@/types/common';
import { FileUploadResponse } from '@/types/upload';
import { FileType } from '@/types/enums';

export interface UploadParams {
  file: File;
  targetType: 'HORSE' | 'JOCKEY' | 'HEALTH_RECORD' | 'DOPING_TEST';
  fileType: FileType;
  targetId?: string;
  onUploadProgress?: (progressEvent: { loaded: number; total?: number }) => void;
}

export const uploadApi = {
  upload: ({ file, targetType, fileType, targetId, onUploadProgress }: UploadParams) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('targetType', targetType);
    formData.append('fileType', fileType);
    if (targetId) formData.append('targetId', targetId);

    return axiosInstance.post<ApiResponse<FileUploadResponse>>('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    });
  },

  getById: (id: string) =>
    axiosInstance.get(`/upload/${id}`),

  list: (params: { fileType?: string; targetType?: string; targetId?: string }) =>
    axiosInstance.get<ApiResponse<FileUploadResponse[]>>('/upload', { params }),
};