import { NotificationType } from './enums';

export interface NotificationResponse {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: NotificationType;
  isRead: boolean;
  targetType?: string;
  targetId?: string;
  createdAt: string;
}