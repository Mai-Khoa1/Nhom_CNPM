import { RegistrationStatus } from './enums';

export interface RegistrationCreateRequest {
  raceId: string;
  horseId: string;
  jockeyId: string;
}

export interface RegistrationResponse {
  id: string;
  raceId: string;
  raceName: string;
  raceDate: string;
  horseId: string;
  horseName: string;
  horseCode: string;
  jockeyId: string;
  jockeyName: string;
  ownerId: string;
  ownerName: string;
  laneNumber?: number;
  status: RegistrationStatus;
  rejectReason?: string;
  registeredAt: string;
}