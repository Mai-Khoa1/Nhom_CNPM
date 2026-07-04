export interface ResultDetailItem {
  registrationId: string;
  finishPosition: number;
  finishTime?: string;
  notes?: string;
}

export interface ResultEntryRequest {
  raceId: string;
  details: ResultDetailItem[];
  confirmEditPublished?: boolean;
}

export interface ResultDetailResponse {
  finishPosition: number;
  horseId: string;
  horseName: string;
  horseCode: string;
  jockeyId: string;
  jockeyName: string;
  laneNumber?: number;
  finishTime?: string;
  pointsEarned: number;
  notes?: string;
}

export interface ResultResponse {
  id: string;
  raceId: string;
  raceName: string;
  isPublished: boolean;
  publishedAt?: string;
  details: ResultDetailResponse[];
}