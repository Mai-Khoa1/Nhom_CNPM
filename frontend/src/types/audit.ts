import { AuditAction } from './enums';

export interface AuditLogResponse {
  id: number;
  userId?: number;
  username?: string;
  action: AuditAction;
  targetType?: string;
  targetId?: number;
  description?: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}