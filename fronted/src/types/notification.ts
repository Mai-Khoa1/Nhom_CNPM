import { NotificationType } from './enums';

export interface NotificationResponse {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  targetType?: string;
  targetId?: number;
  createdAt: string;
}