import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { dashboardApi } from '@/api/dashboardApi';
import { queryKeys } from '@/constants/queryKeys';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Trophy, Flag, Users, Calendar } from 'lucide-react';
import { formatDateTime } from '@/utils/formatDate';
import { getRaceStatusColor } from '@/utils/getStatusColor';
import { StatusBadge } from '@/components/common/StatusBadge';

const HomePage = () => {
  const { data: statsData, isLoading: statsLoading } = useQuery({
    queryKey: queryKeys.dashboard.stats,
    queryFn: () => dashboardApi.getStats(),
  });

  const { data: racesData, isLoading: racesLoading } = useQuery({
    queryKey: queryKeys.races.list({ page: 0, size: 6 }),
    queryFn: () => raceApi.getAll({ page: 0, size: 6 }),
  });

  const stats = statsData?.data?.data;
  const races = racesData?.data?.data?.content;

  return (
    <div>
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-[#1A2B4A] to-[#243756] text-white py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-5xl font-bold mb-4">
            <span className="text-[#D4A017]">🏇 Giải Đua Ngựa</span> Chuyên Nghiệp
          </h1>
          <p className="text-lg text-gray-300 mb-8 max-w-2xl mx-auto">
            Hệ thống quản lý giải đua ngựa hiện đại - Theo dõi cuộc đua, xếp hạng và kết quả trực tiếp
          </p>
          <div className="flex gap-4 justify-center">
            <Link to="/races">
              <Button size="lg" className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
                Xem cuộc đua
              </Button>
            </Link>
            <Link to="/leaderboard">
              <Button size="lg" variant="outline" className="border-[#D4A017] text-[#D4A017] hover:bg-[#D4A017] hover:text-white">
                Bảng xếp hạng
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="container mx-auto px-4 -mt-8">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {statsLoading ? (
            Array.from({ length: 4 }).map((_, i) => (
              <Card key={i}><CardContent className="p-6"><Skeleton className="h-16" /></CardContent></Card>
            ))
          ) : (
            <>
              <Card className="shadow-lg">
                <CardContent className="p-6 flex items-center gap-4">
                  <div className="p-3 rounded-full bg-[#D4A017]/10"><Trophy className="h-6 w-6 text-[#D4A017]" /></div>
                  <div>
                    <p className="text-2xl font-bold">{stats?.totalSeasons ?? '-'}</p>
                    <p className="text-sm text-muted-foreground">Mùa giải</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="shadow-lg">
                <CardContent className="p-6 flex items-center gap-4">
                  <div className="p-3 rounded-full bg-blue-100 dark:bg-blue-900/30"><Flag className="h-6 w-6 text-blue-600" /></div>
                  <div>
                    <p className="text-2xl font-bold">{stats?.totalRaces ?? '-'}</p>
                    <p className="text-sm text-muted-foreground">Cuộc đua</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="shadow-lg">
                <CardContent className="p-6 flex items-center gap-4">
                  <div className="p-3 rounded-full bg-green-100 dark:bg-green-900/30"><span className="text-xl">🐴</span></div>
                  <div>
                    <p className="text-2xl font-bold">{stats?.totalHorses ?? '-'}</p>
                    <p className="text-sm text-muted-foreground">Ngựa đua</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="shadow-lg">
                <CardContent className="p-6 flex items-center gap-4">
                  <div className="p-3 rounded-full bg-purple-100 dark:bg-purple-900/30"><Users className="h-6 w-6 text-purple-600" /></div>
                  <div>
                    <p className="text-2xl font-bold">{stats?.totalJockeys ?? '-'}</p>
                    <p className="text-sm text-muted-foreground">Nài ngựa</p>
                  </div>
                </CardContent>
              </Card>
            </>
          )}
        </div>
      </section>

      {/* Upcoming Races */}
      <section className="container mx-auto px-4 py-12">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold text-[#1A2B4A] dark:text-white">Cuộc đua sắp tới</h2>
          <Link to="/races">
            <Button variant="outline">Xem tất cả</Button>
          </Link>
        </div>
        {racesLoading ? (
          <div className="grid md:grid-cols-3 gap-4">
            {Array.from({ length: 3 }).map((_, i) => (
              <Card key={i}><CardContent className="p-6"><Skeleton className="h-32" /></CardContent></Card>
            ))}
          </div>
        ) : races && races.length > 0 ? (
          <div className="grid md:grid-cols-3 gap-4">
            {races.map((race) => (
              <Link key={race.id} to={`/races/${race.id}`}>
                <Card className="hover:shadow-lg transition-shadow cursor-pointer h-full">
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">{race.name}</CardTitle>
                      <StatusBadge status={race.status} colorClass={getRaceStatusColor(race.status)} />
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2 text-sm text-muted-foreground">
                      <div className="flex items-center gap-2">
                        <Calendar className="h-4 w-4" />
                        <span>{formatDateTime(race.raceDate)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Flag className="h-4 w-4" />
                        <span>{race.location} • {race.distance}m</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Users className="h-4 w-4" />
                        <span>{race.registeredCount}/{race.maxHorses} ngựa</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
        ) : (
          <Card>
            <CardContent className="p-12 text-center text-muted-foreground">
              Chưa có cuộc đua nào
            </CardContent>
          </Card>
        )}
      </section>
    </div>
  );
};

export default HomePage;