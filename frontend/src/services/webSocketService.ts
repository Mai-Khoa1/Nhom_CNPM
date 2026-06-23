import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ENV } from '@/config/env';

let stompClient: Client | null = null;

export const webSocketService = {
  connect: (
    accessToken: string,
    onPersonalNotification: (msg: IMessage) => void,
    onBroadcast: (msg: IMessage) => void,
  ) => {
    stompClient = new Client({
      webSocketFactory: () => new SockJS(ENV.WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        stompClient?.subscribe('/user/queue/notifications', onPersonalNotification);
        stompClient?.subscribe('/topic/notifications', onBroadcast);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });
    stompClient.activate();
  },

  disconnect: () => {
    stompClient?.deactivate();
    stompClient = null;
  },

  isConnected: () => stompClient?.connected ?? false,
};