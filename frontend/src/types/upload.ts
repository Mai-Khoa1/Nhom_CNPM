import { FileType } from './enums';

export interface FileUploadResponse {
  fileId: string;
  url: string;
  fileName: string;
  fileType: string;
  fileCategory: FileType;
  fileSize: number;
  /** "DANG_KY" - tệp tin gắn với 1 lần đăng ký thi đấu cụ thể (targetId = maDangKy). */
  targetType: string | null;
  targetId: string | null;
  ownerId: string | null;
  ownerName: string | null;
  createdAt: string;
}

export interface FileUpdateRequest {
  tenFile: string;
  loaiFile?: string;
  targetType?: string;
  targetId?: string;
}
