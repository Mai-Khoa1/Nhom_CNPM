import { Gender, HorseStatus } from './enums';

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
  status: HorseStatus;
  ownerId: string;
  ownerName: string;
  createdAt: string;
}

export interface HorseListParams {
  page?: number;
  size?: number;
  sort?: string;
  keyword?: string;
  status?: HorseStatus;
  ownerId?: string;
}