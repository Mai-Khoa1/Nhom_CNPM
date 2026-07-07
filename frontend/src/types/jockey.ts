import { Gender } from './enums';

export interface JockeyCreateRequest {
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  experienceYears?: number;
  weight?: number;
  licenseNumber?: string;
}

export type JockeyUpdateRequest = Partial<JockeyCreateRequest>;

export interface JockeyResponse {
  id: string;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  experienceYears?: number;
  weight?: number;
  licenseNumber?: string;
  ownerId: string;
  ownerName: string;
  avatarUrl?: string;
  licenseScanUrl?: string;
  medicalCertUrl?: string;
  /** false = "Ngừng hoạt động" (xóa mềm - hồ sơ đã từng có đăng ký thi đấu nên không xóa cứng được). */
  active: boolean;
  createdAt: string;
}

export interface JockeyListParams {
  page?: number;
  size?: number;
  sort?: string;
  keyword?: string;
  ownerId?: string;
  includeInactive?: boolean;
}