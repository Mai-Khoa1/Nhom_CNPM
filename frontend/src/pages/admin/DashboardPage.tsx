import { useQuery } from '@tanstack/react-query';
import { dashboardApi } from '@/api/dashboardApi';
import { queryKeys } from '@/constants/queryKeys';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRaceStatusColor } from '@/utils/getStatusColor';
import { formatDateTime } from '@/utils/formatDate';
import { Trophy, Flag, Users, ClipboardList, AlertCircle } from 'lucide-react';

const DashboardPage = () => {
  const { data, isLoading, isError } = useQuery({
    queryKey: queryKeys.dashboard.stats,
    queryFn: () => dashboardApi.getStats(),
  });

  const { data: upcomingData, isLoading: upcomingLoading } = useQuery({
    queryKey: ['dashboard', 'upcoming'],
    queryFn: () => dashboardApi.getUpcoming(),
  });

  const stats = data?.data?.data;
  const upcomingRaces = upcomingData?.data?.data ?? [];

  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <AlertCircle className="h-12 w-12 text-red-500 mb-4" />
        <p className="text-lg font-medium">Không thể tải dữ liệu Dashboard</p>
        <p className="text-sm text-muted-foreground">Vui lòng kiểm tra kết nối Backend</p>
      </div>
    );
  }

  const statCards = [
    { label: 'Tổng ngựa', value: stats?.totalHorses, icon: <span className="text-2xl">🐴</span>, color: 'bg-amber-50 dark:bg-amber-900/20' },
    { label: 'Tổng nài', value: stats?.totalJockeys, icon: <span className="text-2xl">🏇</span>, color: 'bg-blue-50 dark:bg-blue-900/20' },
    { label: 'Tổng cuộc đua', value: stats?.totalRaces, icon: <Flag className="h-6 w-6 text-green-600" />, color: 'bg-green-50 dark:bg-green-900/20' },
    { label: 'Mùa giải', value: stats?.totalSeasons, icon: <Trophy className="h-6 w-6 text-purple-600" />, color: 'bg-purple-50 dark:bg-purple-900/20' },
    { label: 'Ngựa chờ duyệt', value: stats?.pendingHorses, icon: <AlertCircle className="h-6 w-6 text-yellow-600" />, color: 'bg-yellow-50 dark:bg-yellow-900/20' },
    { label: 'Nài chờ duyệt', value: stats?.pendingJockeys, icon: <Users className="h-6 w-6 text-orange-600" />, color: 'bg-orange-50 dark:bg-orange-900/20' },
    { label: 'Đăng ký chờ duyệt', value: stats?.pendingRegistrations, icon: <ClipboardList className="h-6 w-6 text-red-600" />, color: 'bg-red-50 dark:bg-red-900/20' },
    { label: 'Cuộc đua sắp tới', value: stats?.upcomingRaces, icon: <Flag className="h-6 w-6 text-indigo-600" />, color: 'bg-indigo-50 dark:bg-indigo-900/20' },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {isLoading
          ? Array.from({ length: 8 }).map((_, i) => (
              <Card key={i}><CardContent className="p-6"><Skeleton className="h-16" /></CardContent></Card>
            ))
          : statCards.map((card) => (
              <Card key={card.label} className={card.color}>
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-muted-foreground">{card.label}</p>
                      <p className="text-3xl font-bold mt-1">{card.value ?? '-'}</p>
                    </div>
                    {card.icon}
                  </div>
                </CardContent>
              </Card>
            ))
        }
      </div>

      <div className="grid md:grid-cols-2 gap-6 mt-8">
        <Card>
          <CardHeader><CardTitle>Cuộc đua sắp diễn ra</CardTitle></CardHeader>
          <CardContent>
            {upcomingLoading ? (
              <div className="space-y-2">
                {Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-10" />)}
              </div>
            ) : upcomingRaces.length === 0 ? (
              <p className="text-sm text-muted-foreground">Không có cuộc đua nào sắp diễn ra</p>
            ) : (
              <div className="space-y-2">
                {upcomingRaces.map((race) => (
                  <div key={race.id} className="flex items-center justify-between p-2 rounded-lg bg-muted/50">
                    <div className="min-w-0">
                      <p className="text-sm font-medium truncate">{race.name}</p>
                      <p className="text-xs text-muted-foreground">{race.location} · {race.raceDate ? formatDateTime(race.raceDate) : '?'}</p>
                    </div>
                    <StatusBadge status={race.status} colorClass={getRaceStatusColor(race.status)} />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Hoạt động gần đây</CardTitle></CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">Xem chi tiết tại trang Audit Log</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default DashboardPage;