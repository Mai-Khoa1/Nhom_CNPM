import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { notificationApi } from '@/api/notificationApi';
import { queryKeys } from '@/constants/queryKeys';
import { useAuthStore } from '@/store/authStore';
import { useNotificationStore } from '@/store/notificationStore';

/**
 * Đồng bộ số huy hiệu thông báo chưa đọc từ backend (nguồn dữ liệu chính thức),
 * không chỉ dựa vào WebSocket. Tự động cập nhật lại sau khi đánh dấu đã đọc nhờ
 * invalidateQueries(['notifications']) ở các trang thông báo (khớp queryKeys.notifications.unreadCount).
 */
export const useNotificationBadge = () => {
  const { isAuthenticated } = useAuthStore();
  const setUnreadCount = useNotificationStore((s) => s.setUnreadCount);

  const { data } = useQuery({
    queryKey: queryKeys.notifications.unreadCount,
    queryFn: () => notificationApi.getUnreadCount(),
    enabled: isAuthenticated,
    refetchInterval: 30000,
  });

  useEffect(() => {
    const count = data?.data?.data?.count;
    if (count !== undefined) {
      setUnreadCount(count);
    }
  }, [data, setUnreadCount]);
};
