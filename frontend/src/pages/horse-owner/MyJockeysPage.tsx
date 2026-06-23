import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { JockeyStatus } from '@/types/enums';
import { JockeyResponse } from '@/types/jockey';
import { Button } from '@/components/ui/button';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { getJockeyStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, Eye, Edit, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

const MyJockeysPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [deleteId, setDeleteId] = useState<string | null>(null);

  const params = { page, size: 10 };

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

  const jockeys = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<JockeyResponse>[] = [
    { key: 'fullName', header: 'Họ tên', render: (j) => <span className="font-medium">{j.fullName}</span> },
    { key: 'gender', header: 'Giới tính' },
    { key: 'dateOfBirth', header: 'Ngày sinh', render: (j) => formatDate(j.dateOfBirth) },
    { key: 'experienceYears', header: 'Kinh nghiệm', render: (j) => j.experienceYears ? `${j.experienceYears} năm` : '-' },
    { key: 'weight', header: 'Cân nặng', render: (j) => j.weight ? `${j.weight} kg` : '-' },
    {
      key: 'status', header: 'Trạng thái',
      render: (j) => <StatusBadge status={j.status} colorClass={getJockeyStatusColor(j.status)} />,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (j) => (
        <div className="flex items-center gap-1">
          <Link to={`/my-jockeys/${j.id}`}>
            <Button variant="ghost" size="icon"><Eye className="h-4 w-4" /></Button>
          </Link>
          {j.status === JockeyStatus.PENDING && (
            <Link to={`/my-jockeys/${j.id}/edit`}>
              <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
            </Link>
          )}
          {j.status === JockeyStatus.PENDING && (
            <Button variant="ghost" size="icon" onClick={() => setDeleteId(j.id)}>
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
        <h1 className="text-2xl font-bold">Nài của tôi</h1>
        <Link to="/my-jockeys/create">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
            <Plus className="h-4 w-4 mr-2" />Thêm nài
          </Button>
        </Link>
      </div>

      <DataTable columns={columns} data={jockeys ?? []} isLoading={isLoading} emptyMessage="Bạn chưa có nài nào" />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <ConfirmDialog
        open={deleteId !== null}
        onOpenChange={() => setDeleteId(null)}
        title="Xóa nài"
        description="Bạn có chắc muốn xóa nài này?"
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default MyJockeysPage;