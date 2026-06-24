import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { HorseResponse } from '@/types/horse';
import { HorseStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getHorseStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Check, X, Ban } from 'lucide-react';
import { toast } from 'sonner';

const HorseManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState<HorseStatus | 'ALL'>(HorseStatus.PENDING);
  const [rejectDialog, setRejectDialog] = useState<{ open: boolean; id: string | null }>({ open: false, id: null });
  const [reason, setReason] = useState('');

  const params = { page, size: 10, status: statusFilter === 'ALL' ? undefined : statusFilter };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.list(params),
    queryFn: () => horseApi.getAll(params),
  });

  const approveMutation = useMutation({
    mutationFn: (id: string) => horseApi.approve(id),
    onSuccess: () => {
      toast.success('Đã duyệt ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => horseApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Đã từ chối ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
      setRejectDialog({ open: false, id: null });
      setReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const disqualifyMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => horseApi.disqualify(id, reason),
    onSuccess: () => {
      toast.success('Đã loại ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const horses = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<HorseResponse>[] = [
    { key: 'code', header: 'Mã ngựa' },
    { key: 'name', header: 'Tên ngựa', render: (h) => <span className="font-medium">{h.name}</span> },
    { key: 'breed', header: 'Giống' },
    { key: 'ownerName', header: 'Chủ sở hữu' },
    { key: 'createdAt', header: 'Ngày tạo', render: (h) => formatDate(h.createdAt) },
    {
      key: 'status', header: 'Trạng thái',
      render: (h) => <StatusBadge status={h.status} colorClass={getHorseStatusColor(h.status)} />,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (h) => h.status === HorseStatus.PENDING ? (
        <div className="flex items-center gap-1">
          <Button size="sm" variant="ghost" className="text-green-600" onClick={() => approveMutation.mutate(h.id)}>
            <Check className="h-4 w-4" />
          </Button>
          <Button size="sm" variant="ghost" className="text-red-600" onClick={() => setRejectDialog({ open: true, id: h.id })}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : h.status === HorseStatus.APPROVED ? (
        <Button size="sm" variant="ghost" className="text-gray-600" onClick={() => setRejectDialog({ open: true, id: h.id })}>
          <Ban className="h-4 w-4" />
        </Button>
      ) : null,
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý Ngựa</h1>
        <Select value={statusFilter} onValueChange={(v) => { setStatusFilter(v as HorseStatus | 'ALL'); setPage(0); }}>
          <SelectTrigger className="w-48"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            <SelectItem value={HorseStatus.PENDING}>Chờ duyệt</SelectItem>
            <SelectItem value={HorseStatus.APPROVED}>Đã duyệt</SelectItem>
            <SelectItem value={HorseStatus.REJECTED}>Bị từ chối</SelectItem>
            <SelectItem value={HorseStatus.DISQUALIFIED}>Bị loại</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={horses ?? []} isLoading={isLoading} emptyMessage="Không có ngựa nào" />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={rejectDialog.open} onOpenChange={(open) => setRejectDialog({ open, id: open ? rejectDialog.id : null })}>
        <DialogContent>
          <DialogHeader><DialogTitle>Lý do</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div>
              <Label>Lý do từ chối / loại *</Label>
              <Input value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Nhập lý do..." />
            </div>
            <div className="flex gap-2">
              <Button
                className="flex-1 bg-red-600 hover:bg-red-700 text-white"
                disabled={!reason.trim()}
                onClick={() => rejectDialog.id && rejectMutation.mutate({ id: rejectDialog.id, reason })}
              >
                Từ chối
              </Button>
              <Button
                className="flex-1 bg-gray-600 hover:bg-gray-700 text-white"
                disabled={!reason.trim()}
                onClick={() => rejectDialog.id && disqualifyMutation.mutate({ id: rejectDialog.id, reason })}
              >
                Loại
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default HorseManagementPage;
