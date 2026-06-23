import { FileType } from './enums';

export interface FileUploadResponse {
  fileId: string;
  url: string;
  fileName: string;
  fileType: string;
  fileCategory: FileType;
  fileSize: number;
}