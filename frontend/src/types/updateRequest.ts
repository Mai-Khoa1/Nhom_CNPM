import { UpdateRequestStatus, UpdateTargetType } from './enums';

export interface UpdateRequestResponse {
  id: string;
  targetType: UpdateTargetType;
  targetId: string;
  targetName: string;
  ownerId: string;
  ownerName: string;
  oldData: Record<string, unknown>;
  newData: Record<string, unknown>;
  status: UpdateRequestStatus;
  rejectReason?: string;
  createdAt: string;
  processedAt?: string;
}
