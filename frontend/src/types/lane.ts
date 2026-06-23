export interface LaneAssignRequest {
  registrationId: string;
  laneNumber: number;
}

export interface LaneResponse {
  id: string;
  raceId: string;
  registrationId: string;
  horseName: string;
  jockeyName: string;
  laneNumber: number;
  assignedAt: string;
}