import { RaceStatus } from './enums';

export interface RaceCreateRequest {
  seasonId: string;
  name: string;
  raceDate: string;
  location: string;
  distance: number;
  maxHorses: number;
  minHorses?: number;
  description?: string;
}

export type RaceUpdateRequest = Partial<RaceCreateRequest>;

export interface RaceResponse {
  id: string;
  seasonId: string;
  seasonName: string;
  name: string;
  raceDate: string;
  location: string;
  distance: number;
  maxHorses: number;
  minHorses: number;
  registeredCount: number;
  approvedCount: number;
  status: RaceStatus;
  description?: string;
  createdAt: string;
}