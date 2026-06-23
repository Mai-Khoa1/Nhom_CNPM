export interface HorseRankingResponse {
  id: string;
  seasonId: string;
  horseId: string;
  horseName: string;
  horseCode: string;
  ownerName: string;
  totalPoints: number;
  totalRaces: number;
  totalWins: number;
  updatedAt: string;
}

export interface JockeyRankingResponse {
  id: string;
  seasonId: string;
  jockeyId: string;
  jockeyName: string;
  ownerName: string;
  totalPoints: number;
  totalRaces: number;
  totalWins: number;
  updatedAt: string;
}