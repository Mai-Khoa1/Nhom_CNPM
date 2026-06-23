import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceResponse } from '@/types/race';
import { RaceStatus } from '@/types/enums';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { SearchBar } from '@/components/common/SearchBar';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRaceStatusColor } from '@/utils/getStatusColor';
import { formatDateTime } from '@/utils/formatDate';
import { useDebounce } from '@/hooks/useDebounce';

const RaceManagementPage = () => {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const debouncedKeyword = useDebounce(keyword);

  const params = {
    page,
    size: 10,
    keyword: debouncedKeyword || undefined,
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.races.list(params),
    queryFn: () => raceApi.getAll(params),
  });

  const races = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<RaceResponse>[] = [
    { key: 'name', header: 'Tên cuộc đua', render: (r) => <span className="font-medium">{r.name}</span> },
    { key: 'seasonName', header: 'Mùa giải' },
    { key: 'raceDate', header: 'Ngày đua', render: (r) => formatDateTime(r.raceDate) },
    { key: 'location', header: 'Địa điểm' },
    { key: 'distance', header: 'Cự ly', render: (r) => `${r.distance}m` },
    { key: 'registered', header: 'Đăng ký', render: (r) => `${r.registeredCount}/${r.maxHorses}` },
    {
      key: 'status', header: 'Trạng thái',
      render: (r) => <StatusBadge status={r.status} colorClass={getRaceStatusColor(r.status)} />,
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Quản lý Cuộc đua</h1>

      <div className="flex flex-col md:flex-row gap-4 mb-4">
        <div className="flex-1">
          <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm cuộc đua..." />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[160px]">
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

      <DataTable columns={columns} data={races ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}
    </div>
  );
};

export default RaceManagementPage;