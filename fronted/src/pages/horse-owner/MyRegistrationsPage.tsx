import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { RegistrationResponse } from '@/types/registration';
import { Button } from '@/components/ui/button';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRegistrationStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { Plus } from 'lucide-react';

const MyRegistrationsPage = () => {
  const [page, setPage] = useState(0);
  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.registrations.my(params),
    queryFn: () => registrationApi.getMy(params),
  });

  const registrations = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<RegistrationResponse>[] = [
    { key: 'raceName', header: 'Cuộc đua', render: (r) => <span className="font-medium">{r.raceName}</span> },
    { key: 'raceDate', header: 'Ngày đua', render: (r) => formatDate(r.raceDate) },
    { key: 'horseName', header: 'Ngựa' },
    { key: 'jockeyName', header: 'Nài' },
    { key: 'laneNumber', header: 'Làn', render: (r) => r.laneNumber ?? '-' },
    {
      key: 'status', header: 'Trạng thái',
      render: (r) => <StatusBadge status={r.status} colorClass={getRegistrationStatusColor(r.status)} />,
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Đăng ký thi đấu</h1>
        <Link to="/my-registrations/new">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
            <Plus className="h-4 w-4 mr-2" />Đăng ký mới
          </Button>
        </Link>
      </div>

      <DataTable columns={columns} data={registrations ?? []} isLoading={isLoading} emptyMessage="Bạn chưa có đăng ký nào" />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}
    </div>
  );
};

export default MyRegistrationsPage;