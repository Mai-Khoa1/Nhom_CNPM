import { useEffect } from 'react';
import { Rabbit, Users, Calendar, Trophy } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { useAppDispatch, useAppSelector, fetchHorses, fetchJockeys, fetchSchedules, fetchResults } from '@/store';

export default function Dashboard() {
  const dispatch = useAppDispatch();
  const horses = useAppSelector((state) => state.horses.items);
  const jockeys = useAppSelector((state) => state.jockeys.items);
  const schedules = useAppSelector((state) => state.schedules.items);
  const results = useAppSelector((state) => state.results.items);

  useEffect(() => {
    dispatch(fetchHorses());
    dispatch(fetchJockeys());
    dispatch(fetchSchedules());
    dispatch(fetchResults());
  }, [dispatch]);

  const activeHorses = horses.filter((h) => h.status === 'active').length;
  const upcomingSchedules = schedules.filter((s) => s.status === 'upcoming').length;

  const statsCards = [
    { title: 'Tổng số ngựa', value: horses.length, sub: `${activeHorses} đang hoạt động`, icon: Rabbit, color: 'text-emerald-500', bg: 'bg-emerald-500/10' },
    { title: 'Tổng kỵ sĩ', value: jockeys.length, sub: 'Đã đăng ký', icon: Users, color: 'text-blue-500', bg: 'bg-blue-500/10' },
    { title: 'Lịch đua', value: schedules.length, sub: `${upcomingSchedules} sắp diễn ra`, icon: Calendar, color: 'text-amber-500', bg: 'bg-amber-500/10' },
    { title: 'Kết quả', value: results.length, sub: 'Cuộc đua đã hoàn thành', icon: Trophy, color: 'text-purple-500', bg: 'bg-purple-500/10' },
  ];

  const recentResults = [...results].slice(0, 5);
  const upcomingRaces = schedules.filter((s) => s.status === 'upcoming').slice(0, 5);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-gray-900 dark:text-white">Dashboard</h1>
        <p className="text-muted-foreground mt-1">Tổng quan hệ thống quản lý đua ngựa</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statsCards.map((stat) => (
          <Card key={stat.title}>
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <div className={`p-2 rounded-lg ${stat.bg}`}>
                  <stat.icon className={`h-5 w-5 ${stat.color}`} />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">{stat.title}</p>
                  <p className="text-2xl font-bold">{stat.value}</p>
                  <p className="text-xs text-muted-foreground">{stat.sub}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Trophy className="h-5 w-5 text-amber-500" />
              Kết quả gần đây
            </CardTitle>
            <CardDescription>Các cuộc đua đã hoàn thành</CardDescription>
          </CardHeader>
          <CardContent>
            {recentResults.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">Chưa có kết quả nào</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Cuộc đua</TableHead>
                    <TableHead>Người thắng</TableHead>
                    <TableHead>Thời gian</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {recentResults.map((result) => (
                    <TableRow key={result.id}>
                      <TableCell className="font-medium">{result.raceName}</TableCell>
                      <TableCell>{result.winner}</TableCell>
                      <TableCell className="font-mono">{result.time}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="h-5 w-5 text-blue-500" />
              Lịch đua sắp tới
            </CardTitle>
            <CardDescription>Các cuộc đua sắp diễn ra</CardDescription>
          </CardHeader>
          <CardContent>
            {upcomingRaces.length === 0 ? (
              <p className="text-center text-muted-foreground py-4">Chưa có lịch đua nào</p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tên cuộc đua</TableHead>
                    <TableHead>Địa điểm</TableHead>
                    <TableHead>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {upcomingRaces.map((schedule) => (
                    <TableRow key={schedule.id}>
                      <TableCell className="font-medium">{schedule.name}</TableCell>
                      <TableCell>{schedule.venue}</TableCell>
                      <TableCell>
                        <Badge variant="outline" className="bg-blue-500/20 text-blue-400 border-blue-500/30">
                          {schedule.status}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}