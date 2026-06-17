import { RegistrationStatus } from './enums';

export interface RegistrationCreateRequest {
  raceId: number;
  horseId: number;
  jockeyId: number;
}

export interface RegistrationResponse {
  id: number;
  raceId: number;
  raceName: string;
  raceDate: string;
  horseId: number;
  horseName: string;
  horseCode: string;
  jockeyId: number;
  jockeyName: string;
  ownerId: number;
  ownerName: string;
  laneNumber?: number;
  status: RegistrationStatus;
  rejectReason?: string;
  registeredAt: string;
}