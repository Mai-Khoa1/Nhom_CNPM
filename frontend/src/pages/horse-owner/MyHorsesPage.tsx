import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { HorseStatus } from '@/types/enums';
import { HorseResponse } from '@/types/horse';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { SearchBar } from '@/components/common/SearchBar';
import { PaginationControl } from '@/components/common/PaginationControl';
import { DataTable, Column } from '@/components/common/DataTable';
import { StatusBadge } from '@/components/common/StatusBadge';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { getHorseStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { useDebounce } from '@/hooks/useDebounce';
import { Plus, Eye, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

const MyHorsesPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const debouncedKeyword = useDebounce(keyword);

  const params: Record<string, unknown> = {
    page,
    size: 10,
    keyword: debouncedKeyword || undefined,
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.list(params),
    queryFn: () => horseApi.getAll(params as never),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => horseApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
      setDeleteId(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const horses = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<HorseResponse>[] = [
    { key: 'code', header: 'Mã' },
    { key: 'name', header: 'Tên ngựa', render: (h) => <span className="font-medium">{h.name}</span> },
    { key: 'breed', header: 'Giống' },
    { key: 'gender', header: 'Giới tính' },
    { key: 'dateOfBirth', header: 'Ngày sinh', render: (h) => formatDate(h.dateOfBirth) },
    {
      key: 'status', header: 'Trạng thái',
      render: (h) => <StatusBadge status={h.status} colorClass={getHorseStatusColor(h.status)} />,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (h) => (
        <div className="flex items-center gap-1">
          <Link to={`/my-horses/${h.id}`}>
            <Button variant="ghost" size="icon"><Eye className="h-4 w-4" /></Button>
          </Link>
          {h.status === HorseStatus.PENDING && (
            <Link to={`/my-horses/${h.id}/edit`}>
              <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
            </Link>
          )}
          {h.status === HorseStatus.PENDING && (
            <Button variant="ghost" size="icon" onClick={() => setDeleteId(h.id)}>
              <Trash2 className="h-4 w-4 text-red-500" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Ngựa của tôi</h1>
        <Link to="/my-horses/create">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
            <Plus className="h-4 w-4 mr-2" />Thêm ngựa
          </Button>
        </Link>
      </div>

      <div className="flex flex-col md:flex-row gap-4 mb-4">
        <div className="flex-1">
          <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm theo tên hoặc mã ngựa..." />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            {Object.values(HorseStatus).map((s) => (
              <SelectItem key={s} value={s}>{s}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={horses ?? []} isLoading={isLoading} emptyMessage="Bạn chưa có ngựa nào" />

      {pageInfo && (
        <PaginationControl
          page={pageInfo.page}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={setPage}
        />
      )}

      <ConfirmDialog
        open={deleteId !== null}
        onOpenChange={() => setDeleteId(null)}
        title="Xóa ngựa"
        description="Bạn có chắc muốn xóa ngựa này? Hành động không thể hoàn tác."
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default MyHorsesPage;