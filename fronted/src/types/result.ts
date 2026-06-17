export interface ResultDetailItem {
  registrationId: number;
  finishPosition: number;
  finishTime?: string;
  notes?: string;
}

export interface ResultEntryRequest {
  raceId: number;
  details: ResultDetailItem[];
}

export interface ResultDetailResponse {
  finishPosition: number;
  horseId: number;
  horseName: string;
  horseCode: string;
  jockeyId: number;
  jockeyName: string;
  laneNumber?: number;
  finishTime?: string;
  pointsEarned: number;
  notes?: string;
}

export interface ResultResponse {
  id: number;
  raceId: number;
  raceName: string;
  isPublished: boolean;
  publishedAt?: string;
  details: ResultDetailResponse[];
}