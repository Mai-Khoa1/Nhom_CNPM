import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceStatus } from '@/types/enums';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { SearchBar } from '@/components/common/SearchBar';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { EmptyState } from '@/components/common/EmptyState';
import { getRaceStatusColor } from '@/utils/getStatusColor';
import { formatDateTime } from '@/utils/formatDate';
import { useDebounce } from '@/hooks/useDebounce';
import { Calendar, Flag, Users } from 'lucide-react';

const RaceListPage = () => {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const debouncedKeyword = useDebounce(keyword);

  const params = {
    page,
    size: 9,
    keyword: debouncedKeyword || undefined,
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.races.list(params),
    queryFn: () => raceApi.getAll(params),
  });

  const races = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-[#1A2B4A] dark:text-white mb-6">Danh sách cuộc đua</h1>

      <div className="flex flex-col md:flex-row gap-4 mb-6">
        <div className="flex-1">
          <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm kiếm cuộc đua..." />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            {Object.values(RaceStatus).map((s) => (
              <SelectItem key={s} value={s}>{s}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-12" text="Đang tải danh sách cuộc đua..." />
      ) : races && races.length > 0 ? (
        <>
          <div className="grid md:grid-cols-3 gap-4">
            {races.map((race) => (
              <Link key={race.id} to={`/races/${race.id}`}>
                <Card className="hover:shadow-lg transition-shadow cursor-pointer h-full">
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-lg">{race.name}</CardTitle>
                      <StatusBadge status={race.status} colorClass={getRaceStatusColor(race.status)} />
                    </div>
                    <p className="text-sm text-muted-foreground">{race.seasonName}</p>
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
                        <span>{race.registeredCount}/{race.maxHorses} ngựa đăng ký</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
          {pageInfo && (
            <PaginationControl
              page={pageInfo.page}
              totalPages={pageInfo.totalPages}
              totalElements={pageInfo.totalElements}
              onPageChange={setPage}
            />
          )}
        </>
      ) : (
        <EmptyState title="Không tìm thấy cuộc đua" description="Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm" />
      )}
    </div>
  );
};

export default RaceListPage;