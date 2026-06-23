import { FileType } from './enums';

export interface FileUploadResponse {
  fileId: number;
  url: string;
  fileName: string;
  fileType: string;
  fileCategory: FileType;
  fileSize: number;
}