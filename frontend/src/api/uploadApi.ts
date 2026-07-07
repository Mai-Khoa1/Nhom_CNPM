import { axiosInstance } from './axiosInstance';
import { ApiResponse, PageResponse } from '@/types/common';
import { FileUploadResponse, FileUpdateRequest } from '@/types/upload';
import { FileType } from '@/types/enums';

export interface UploadParams {
  file: File;
  /** "DANG_KY" - tệp tin gắn với 1 lần đăng ký thi đấu cụ thể (targetId = maDangKy). */
  targetType: 'DANG_KY';
  fileType: FileType;
  targetId?: string;
  fileName?: string;
  onUploadProgress?: (progressEvent: { loaded: number; total?: number }) => void;
}

export const uploadApi = {
  upload: ({ file, targetType, fileType, targetId, fileName, onUploadProgress }: UploadParams) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('targetType', targetType);
    formData.append('fileType', fileType);
    if (targetId) formData.append('targetId', targetId);
    if (fileName) formData.append('fileName', fileName);

    return axiosInstance.post<ApiResponse<FileUploadResponse>>('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress,
    });
  },

  list: (params?: Record<string, unknown>) =>
    axiosInstance.get<ApiResponse<PageResponse<FileUploadResponse>>>('/upload', { params }),

  getById: (id: string) =>
    axiosInstance.get(`/upload/${id}`, { responseType: 'blob' }),

  getMeta: (id: string) =>
    axiosInstance.get<ApiResponse<FileUploadResponse>>(`/upload/${id}/meta`),

  update: (id: string, data: FileUpdateRequest) =>
    axiosInstance.put<ApiResponse<FileUploadResponse>>(`/upload/${id}`, data),

  delete: (id: string) =>
    axiosInstance.delete<ApiResponse<void>>(`/upload/${id}`),

  downloadUrl: (id: string) => `/upload/${id}`,
};
