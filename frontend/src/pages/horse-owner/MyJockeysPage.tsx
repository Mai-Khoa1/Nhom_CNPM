import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { JockeyResponse } from '@/types/jockey';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, Eye, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

type ActiveFilter = 'ACTIVE' | 'INACTIVE' | 'ALL';

const MyJockeysPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('ACTIVE');
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const params = { page, size: 10, includeInactive: activeFilter !== 'ACTIVE' };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.jockeys.list(params),
    queryFn: () => jockeyApi.getAll(params),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => jockeyApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa nài');
      queryClient.invalidateQueries({ queryKey: queryKeys.jockeys.all });
      setDeleteId(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const allJockeys = data?.data?.data?.content ?? [];
  const jockeys = activeFilter === 'INACTIVE' ? allJockeys.filter((j) => !j.active) : allJockeys;
  const pageInfo = data?.data?.data;

  const columns: Column<JockeyResponse>[] = [
    { key: 'fullName', header: 'Họ tên', render: (j) => <span className="font-medium">{j.fullName}</span> },
    { key: 'gender', header: 'Giới tính' },
    { key: 'dateOfBirth', header: 'Ngày sinh', render: (j) => formatDate(j.dateOfBirth) },
    { key: 'experienceYears', header: 'Kinh nghiệm', render: (j) => j.experienceYears ? `${j.experienceYears} năm` : '-' },
    { key: 'weight', header: 'Cân nặng', render: (j) => j.weight ? `${j.weight} kg` : '-' },
    {
      key: 'status', header: 'Trạng thái',
      render: (j) => j.active
        ? <span className="text-green-600 text-sm font-medium">Hoạt động</span>
        : <span className="text-muted-foreground text-sm">Ngừng hoạt động</span>,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (j) => (
        <div className="flex items-center gap-1">
          <Link to={`/my-jockeys/${j.id}`}>
            <Button variant="ghost" size="icon"><Eye className="h-4 w-4" /></Button>
          </Link>
          <Link to={`/my-jockeys/${j.id}/edit`}>
            <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
          </Link>
          <Button variant="ghost" size="icon" onClick={() => setDeleteId(j.id)}>
            <Trash2 className="h-4 w-4 text-red-500" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Nài của tôi</h1>
        <Link to="/my-jockeys/create">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
            <Plus className="h-4 w-4 mr-2" />Thêm nài
          </Button>
        </Link>
      </div>

      <div className="flex flex-col md:flex-row gap-4 mb-4">
        <div className="flex-1" />
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

      <DataTable columns={columns} data={jockeys} isLoading={isLoading} emptyMessage="Bạn chưa có nài nào" />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <ConfirmDialog
        open={deleteId !== null}
        onOpenChange={() => setDeleteId(null)}
        title="Xóa nài"
        description="Bạn có chắc muốn xóa nài này? Nếu nài đã từng đăng ký thi đấu, hồ sơ sẽ chuyển sang Ngừng hoạt động (giữ lịch sử) thay vì xóa hẳn."
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default MyJockeysPage;
