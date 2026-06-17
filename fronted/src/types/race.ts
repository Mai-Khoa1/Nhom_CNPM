import { RaceStatus } from './enums';

export interface RaceCreateRequest {
  seasonId: number;
  name: string;
  raceDate: string;
  location: string;
  distance: number;
  maxHorses: number;
  description?: string;
}

export type RaceUpdateRequest = Partial<RaceCreateRequest>;

export interface RaceResponse {
  id: number;
  seasonId: number;
  seasonName: string;
  name: string;
  raceDate: string;
  location: string;
  distance: number;
  maxHorses: number;
  registeredCount: number;
  status: RaceStatus;
  description?: string;
  createdAt: string;
}