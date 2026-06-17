export interface HorseRankingResponse {
  id: number;
  seasonId: number;
  horseId: number;
  horseName: string;
  horseCode: string;
  ownerName: string;
  totalPoints: number;
  totalRaces: number;
  totalWins: number;
  updatedAt: string;
}

export interface JockeyRankingResponse {
  id: number;
  seasonId: number;
  jockeyId: number;
  jockeyName: string;
  ownerName: string;
  totalPoints: number;
  totalRaces: number;
  totalWins: number;
  updatedAt: string;
}