export interface LaneAssignRequest {
  registrationId: number;
  laneNumber: number;
}

export interface LaneResponse {
  id: number;
  raceId: number;
  registrationId: number;
  horseName: string;
  jockeyName: string;
  laneNumber: number;
  assignedAt: string;
}