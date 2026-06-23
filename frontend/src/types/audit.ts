import { AuditAction } from './enums';

export interface AuditLogResponse {
  id: string;
  userId?: string;
  username?: string;
  action: AuditAction;
  targetType?: string;
  targetId?: string;
  description?: string;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}