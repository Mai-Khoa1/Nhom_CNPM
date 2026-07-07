import { Gender } from './enums';

export interface HorseCreateRequest {
  name: string;
  code: string;
  breed: string;
  dateOfBirth: string;
  gender: Gender;
  color?: string;
  weight?: number;
}

export type HorseUpdateRequest = Partial<HorseCreateRequest>;

export interface HorseResponse {
  id: string;
  code: string;
  name: string;
  breed: string;
  dateOfBirth: string;
  gender: Gender;
  color?: string;
  weight?: number;
  avatarUrl?: string;
  passportUrl?: string;
  healthCertUrl?: string;
  ownerId: string;
  ownerName: string;
  /** false = "Ngừng hoạt động" (xóa mềm - hồ sơ đã từng có đăng ký thi đấu nên không xóa cứng được). */
  active: boolean;
  createdAt: string;
}

export interface HorseListParams {
  page?: number;
  size?: number;
  sort?: string;
  keyword?: string;
  ownerId?: string;
  includeInactive?: boolean;
}