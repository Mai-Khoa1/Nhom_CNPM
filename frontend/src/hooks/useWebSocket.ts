import { useEffect, useRef } from 'react';
import { IMessage } from '@stomp/stompjs';
import { webSocketService } from '@/services/webSocketService';
import { useAuthStore } from '@/store/authStore';
import { useNotificationStore } from '@/store/notificationStore';
import { NotificationResponse } from '@/types/notification';
import { toast } from 'sonner';

export const useWebSocket = () => {
  const { accessToken, isAuthenticated } = useAuthStore();
  const { addNotification, incrementUnread } = useNotificationStore();
  const connected = useRef(false);

  useEffect(() => {
    if (!isAuthenticated || !accessToken || connected.current) return;

    const handleNotification = (msg: IMessage) => {
      const notification: NotificationResponse = JSON.parse(msg.body);
      addNotification(notification);
      incrementUnread();
      toast.info(notification.title, { description: notification.message });
    };

    webSocketService.connect(accessToken, handleNotification, handleNotification);
    connected.current = true;

    return () => {
      webSocketService.disconnect();
      connected.current = false;
    };
  }, [isAuthenticated, accessToken, addNotification, incrementUnread]);
};