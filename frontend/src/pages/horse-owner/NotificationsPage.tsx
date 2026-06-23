import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationApi } from '@/api/notificationApi';
import { queryKeys } from '@/constants/queryKeys';
import { NotificationResponse } from '@/types/notification';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { PaginationControl } from '@/components/common/PaginationControl';
import { formatDate } from '@/utils/formatDate';
import { Bell, CheckCheck, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { cn } from '@/utils/cn';

const NotificationsPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.notifications.list({ page, size: 10 }),
    queryFn: () => notificationApi.getAll({ page, size: 10 }),
  });

  const markReadMutation = useMutation({
    mutationFn: (id: number) => notificationApi.markRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllRead(),
    onSuccess: () => {
      toast.success('Đã đánh dấu tất cả là đã đọc');
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
    },
  });

  const notifications = data?.data?.data?.content ?? [];
  const pageInfo = data?.data?.data;

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'APPROVAL': return '✅';
      case 'REJECTION': return '❌';
      case 'REGISTRATION': return '📋';
      case 'RESULT': return '🏆';
      default: return '🔔';
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Thông báo</h1>
        <Button
          variant="outline"
          onClick={() => markAllReadMutation.mutate()}
          disabled={markAllReadMutation.isPending}
        >
          <CheckCheck className="h-4 w-4 mr-2" />
          Đánh dấu tất cả đã đọc
        </Button>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
        </div>
      ) : notifications.length === 0 ? (
        <div className="text-center py-12">
          <Bell className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Không có thông báo nào</p>
        </div>
      ) : (
        <div className="space-y-3">
          {notifications.map((notification: NotificationResponse) => (
            <Card
              key={notification.id}
              className={cn(
                'cursor-pointer transition-colors hover:bg-muted/50',
                !notification.isRead && 'border-l-4 border-l-[#D4A017] bg-[#D4A017]/5'
              )}
              onClick={() => !notification.isRead && markReadMutation.mutate(notification.id)}
            >
              <CardContent className="p-4">
                <div className="flex items-start gap-3">
                  <span className="text-xl">{getTypeIcon(notification.type)}</span>
                  <div className="flex-1 min-w-0">
                    <p className={cn('font-medium', !notification.isRead && 'text-foreground')}>
                      {notification.title}
                    </p>
                    <p className="text-sm text-muted-foreground mt-1">{notification.message}</p>
                    <p className="text-xs text-muted-foreground mt-2">{formatDate(notification.createdAt)}</p>
                  </div>
                  {!notification.isRead && (
                    <span className="h-2 w-2 rounded-full bg-[#D4A017] shrink-0 mt-2" />
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {pageInfo && pageInfo.totalPages > 1 && (
        <PaginationControl
          page={pageInfo.page}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={setPage}
        />
      )}
    </div>
  );
};

export default NotificationsPage;