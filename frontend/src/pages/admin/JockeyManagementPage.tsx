import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { JockeyResponse } from '@/types/jockey';
import { JockeyStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getJockeyStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Check, X } from 'lucide-react';
import { toast } from 'sonner';

const JockeyManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<JockeyStatus | 'ALL'>(JockeyStatus.PENDING);
  const [rejectDialog, setRejectDialog] = useState<{ open: boolean; id: string | null }>({ open: false, id: null });
  const [reason, setReason] = useState('');

  const params = { page, size: 10, status: statusFilter === 'ALL' ? undefined : statusFilter };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.jockeys.list(params),
    queryFn: () => jockeyApi.getAll(params),
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => jockeyApi.approve(id),
    onSuccess: () => {
      toast.success('Đã duyệt jockey');
      queryClient.invalidateQueries({ queryKey: queryKeys.jockeys.all });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => jockeyApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Đã từ chối jockey');
      queryClient.invalidateQueries({ queryKey: queryKeys.jockeys.all });
      setRejectDialog({ open: false, id: null });
      setReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const jockeys = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<JockeyResponse>[] = [
    { key: 'fullName', header: 'Họ tên', render: (j) => <span className="font-medium">{j.fullName}</span> },
    { key: 'licenseNumber', header: 'Số giấy phép', render: (j) => j.licenseNumber || '-' },
    { key: 'experienceYears', header: 'Kinh nghiệm', render: (j) => j.experienceYears ? `${j.experienceYears} năm` : '-' },
    { key: 'ownerName', header: 'Chủ sở hữu' },
    { key: 'createdAt', header: 'Ngày tạo', render: (j) => formatDate(j.createdAt) },
    {
      key: 'status', header: 'Trạng thái',
      render: (j) => <StatusBadge status={j.status} colorClass={getJockeyStatusColor(j.status)} />,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (j) => j.status === JockeyStatus.PENDING ? (
        <div className="flex items-center gap-1">
          <Button size="sm" variant="ghost" className="text-green-600" onClick={() => approveMutation.mutate(j.id)}>
            <Check className="h-4 w-4" />
          </Button>
          <Button size="sm" variant="ghost" className="text-red-600" onClick={() => setRejectDialog({ open: true, id: j.id })}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : null,
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý Nài ngựa</h1>
        <Select value={statusFilter} onValueChange={(v) => { setStatusFilter(v as JockeyStatus | 'ALL'); setPage(0); }}>
          <SelectTrigger className="w-48"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            <SelectItem value={JockeyStatus.PENDING}>Chờ duyệt</SelectItem>
            <SelectItem value={JockeyStatus.APPROVED}>Đã duyệt</SelectItem>
            <SelectItem value={JockeyStatus.REJECTED}>Bị từ chối</SelectItem>
            <SelectItem value={JockeyStatus.ACTIVE}>Đang hoạt động</SelectItem>
            <SelectItem value={JockeyStatus.INACTIVE}>Không hoạt động</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={jockeys ?? []} isLoading={isLoading} emptyMessage="Không có nài ngựa nào" />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={rejectDialog.open} onOpenChange={(open) => setRejectDialog({ open, id: open ? rejectDialog.id : null })}>
        <DialogContent>
          <DialogHeader><DialogTitle>Từ chối jockey</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div>
              <Label>Lý do từ chối *</Label>
              <Input value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Nhập lý do..." />
            </div>
            <Button
              className="w-full bg-red-600 hover:bg-red-700 text-white"
              disabled={!reason.trim()}
              onClick={() => rejectDialog.id && rejectMutation.mutate({ id: rejectDialog.id, reason })}
            >
              Từ chối
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default JockeyManagementPage;
