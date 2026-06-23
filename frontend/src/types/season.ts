import { SeasonStatus } from './enums';

export interface SeasonCreateRequest {
  name: string;
  startDate: string;
  endDate: string;
  description?: string;
}

export type SeasonUpdateRequest = Partial<SeasonCreateRequest>;

export interface SeasonResponse {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: SeasonStatus;
  description?: string;
  createdBy?: number;
  createdAt: string;
}

export interface PointRuleResponse {
  id: number;
  seasonId: number;
  position: number;
  point: number;
}

export interface PointRuleRequest {
  position: number;
  point: number;
}