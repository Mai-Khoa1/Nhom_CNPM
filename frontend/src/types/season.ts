import { SeasonStatus } from './enums';

export interface SeasonCreateRequest {
  name: string;
  startDate: string;
  endDate: string;
  description?: string;
}

export type SeasonUpdateRequest = Partial<SeasonCreateRequest>;

export interface SeasonResponse {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  status: SeasonStatus;
  description?: string;
  createdBy?: string;
  createdAt: string;
}

export interface PointRuleResponse {
  id: string;
  seasonId: string;
  position: number;
  point: number;
}

export interface PointRuleRequest {
  position: number;
  point: number;
}