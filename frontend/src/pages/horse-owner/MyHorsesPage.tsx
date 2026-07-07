import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { HorseResponse } from '@/types/horse';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { SearchBar } from '@/components/common/SearchBar';
import { PaginationControl } from '@/components/common/PaginationControl';
import { DataTable, Column } from '@/components/common/DataTable';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { useDebounce } from '@/hooks/useDebounce';
import { Plus, Eye, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

type ActiveFilter = 'ACTIVE' | 'INACTIVE' | 'ALL';

const MyHorsesPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ACTIVE');
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const debouncedKeyword = useDebounce(keyword);

  const params = {
    page,
    size: 10,
    keyword: debouncedKeyword || undefined,
    includeInactive: activeFilter !== 'ACTIVE',
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.list(params),
    queryFn: () => horseApi.getAll(params),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => horseApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
      setDeleteId(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const allHorses = data?.data?.data?.content ?? [];
  const horses = activeFilter === 'INACTIVE' ? allHorses.filter((h) => !h.active) : allHorses;
  const pageInfo = data?.data?.data;

  const columns: Column<HorseResponse>[] = [
    { key: 'code', header: 'Mã' },
    { key: 'name', header: 'Tên ngựa', render: (h) => <span className="font-medium">{h.name}</span> },
    { key: 'breed', header: 'Giống' },
    { key: 'gender', header: 'Giới tính' },
    { key: 'dateOfBirth', header: 'Ngày sinh', render: (h) => formatDate(h.dateOfBirth) },
    {
      key: 'status', header: 'Trạng thái',
      render: (h) => h.active
        ? <span className="text-green-600 text-sm font-medium">Hoạt động</span>
        : <span className="text-muted-foreground text-sm">Ngừng hoạt động</span>,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (h) => (
        <div className="flex items-center gap-1">
          <Link to={`/my-horses/${h.id}`}>
            <Button variant="ghost" size="icon"><Eye className="h-4 w-4" /></Button>
          </Link>
          <Link to={`/my-horses/${h.id}/edit`}>
            <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
          </Link>
          <Button variant="ghost" size="icon" onClick={() => setDeleteId(h.id)}>
            <Trash2 className="h-4 w-4 text-red-500" />
          </Button>
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
        <Select value={activeFilter} onValueChange={(v) => { setActiveFilter(v as ActiveFilter); setPage(0); }}>
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ACTIVE">Đang hoạt động</SelectItem>
            <SelectItem value="INACTIVE">Ngừng hoạt động</SelectItem>
            <SelectItem value="ALL">Tất cả</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={horses} isLoading={isLoading} emptyMessage="Bạn chưa có ngựa nào" />

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
        description="Bạn có chắc muốn xóa ngựa này? Nếu ngựa đã từng đăng ký thi đấu, hồ sơ sẽ chuyển sang Ngừng hoạt động (giữ lịch sử) thay vì xóa hẳn."
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default MyHorsesPage;