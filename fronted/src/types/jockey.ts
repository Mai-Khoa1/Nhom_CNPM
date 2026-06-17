import { Gender, JockeyStatus } from './enums';

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
  id: number;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  experienceYears?: number;
  weight?: number;
  licenseNumber?: string;
  ownerId: number;
  ownerName: string;
  avatarUrl?: string;
  licenseScanUrl?: string;
  medicalCertUrl?: string;
  status: JockeyStatus;
  createdAt: string;
}