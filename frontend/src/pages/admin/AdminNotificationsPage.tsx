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

const AdminNotificationsPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.notifications.list({ page, size: 20 }),
    queryFn: () => notificationApi.getAll({ page, size: 20 }),
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationApi.markAllRead(),
    onSuccess: () => {
      toast.success('Đã đánh dấu tất cả là đã đọc');
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
    },
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => notificationApi.markRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.notifications.all });
    },
  });

  const notifications = data?.data?.data?.content ?? [];
  const pageInfo = data?.data?.data;

  const getTypeLabel = (type: string) => {
    switch (type) {
      case 'APPROVAL': return { label: 'Duyệt', color: 'bg-green-100 text-green-700' };
      case 'REJECTION': return { label: 'Từ chối', color: 'bg-red-100 text-red-700' };
      case 'REGISTRATION': return { label: 'Đăng ký', color: 'bg-blue-100 text-blue-700' };
      case 'RESULT': return { label: 'Kết quả', color: 'bg-purple-100 text-purple-700' };
      default: return { label: 'Hệ thống', color: 'bg-gray-100 text-gray-700' };
    }
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý thông báo</h1>
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
          {notifications.map((notification: NotificationResponse) => {
            const typeInfo = getTypeLabel(notification.type);
            return (
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
                    <span className={cn('text-xs px-2 py-1 rounded-full font-medium', typeInfo.color)}>
                      {typeInfo.label}
                    </span>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium">{notification.title}</p>
                      <p className="text-sm text-muted-foreground mt-1">{notification.message}</p>
                      <p className="text-xs text-muted-foreground mt-2">{formatDate(notification.createdAt)}</p>
                    </div>
                    {!notification.isRead && (
                      <span className="h-2 w-2 rounded-full bg-[#D4A017] shrink-0 mt-2" />
                    )}
                  </div>
                </CardContent>
              </Card>
            );
          })}
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

export default AdminNotificationsPage;