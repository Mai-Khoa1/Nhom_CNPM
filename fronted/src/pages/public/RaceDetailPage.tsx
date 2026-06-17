import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { queryKeys } from '@/constants/queryKeys';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { StatusBadge } from '@/components/common/StatusBadge';
import { EmptyState } from '@/components/common/EmptyState';
import { getRaceStatusColor, getRegistrationStatusColor } from '@/utils/getStatusColor';
import { formatDateTime } from '@/utils/formatDate';
import { Calendar, MapPin, Ruler, Users } from 'lucide-react';

const RaceDetailPage = () => {
  const { id } = useParams<{ id: string }>();

  const { data, isLoading, isError } = useQuery({
    queryKey: queryKeys.races.detail(Number(id)),
    queryFn: () => raceApi.getById(Number(id)),
    enabled: !!id,
  });

  const { data: registrationsData, isLoading: regLoading } = useQuery({
    queryKey: queryKeys.races.registrations(Number(id)),
    queryFn: () => raceApi.getRegistrations(Number(id)),
    enabled: !!id,
  });

  if (isLoading) return <LoadingSpinner className="py-20" text="Đang tải thông tin cuộc đua..." />;
  if (isError) return <div className="container mx-auto px-4 py-12 text-center text-red-500">Không thể tải thông tin cuộc đua</div>;

  const race = data?.data?.data;
  const registrations = registrationsData?.data?.data?.content;

  if (!race) return null;

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-2">
          <h1 className="text-3xl font-bold text-[#1A2B4A] dark:text-white">{race.name}</h1>
          <StatusBadge status={race.status} colorClass={getRaceStatusColor(race.status)} />
        </div>
        <p className="text-muted-foreground">Mùa giải: {race.seasonName}</p>
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <Card>
          <CardHeader><CardTitle>Thông tin cuộc đua</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-[#D4A017]" />
              <div>
                <p className="font-medium">Thời gian</p>
                <p className="text-sm text-muted-foreground">{formatDateTime(race.raceDate)}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="h-5 w-5 text-[#D4A017]" />
              <div>
                <p className="font-medium">Địa điểm</p>
                <p className="text-sm text-muted-foreground">{race.location}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Ruler className="h-5 w-5 text-[#D4A017]" />
              <div>
                <p className="font-medium">Cự ly</p>
                <p className="text-sm text-muted-foreground">{race.distance} mét</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Users className="h-5 w-5 text-[#D4A017]" />
              <div>
                <p className="font-medium">Số ngựa</p>
                <p className="text-sm text-muted-foreground">{race.registeredCount}/{race.maxHorses}</p>
              </div>
            </div>
            {race.description && (
              <div className="pt-2 border-t">
                <p className="text-sm text-muted-foreground">{race.description}</p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Danh sách đăng ký</CardTitle></CardHeader>
          <CardContent>
            {regLoading ? (
              <LoadingSpinner text="Đang tải..." />
            ) : registrations && registrations.length > 0 ? (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ngựa</TableHead>
                    <TableHead>Nài</TableHead>
                    <TableHead>Làn</TableHead>
                    <TableHead>Trạng thái</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {registrations.map((reg) => (
                    <TableRow key={reg.id}>
                      <TableCell className="font-medium">{reg.horseName}</TableCell>
                      <TableCell>{reg.jockeyName}</TableCell>
                      <TableCell>{reg.laneNumber ?? '-'}</TableCell>
                      <TableCell>
                        <StatusBadge status={reg.status} colorClass={getRegistrationStatusColor(reg.status)} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            ) : (
              <EmptyState description="Chưa có đăng ký nào" />
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default RaceDetailPage;