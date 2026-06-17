import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { RegistrationResponse } from '@/types/registration';
import { RegistrationStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRegistrationStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Check, X } from 'lucide-react';
import { toast } from 'sonner';

const RegistrationManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [rejectDialog, setRejectDialog] = useState<{ open: boolean; id: number | null }>({ open: false, id: null });
  const [rejectReason, setRejectReason] = useState('');

  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.registrations.list(params),
    queryFn: () => registrationApi.getAll(params),
  });

  const approveMutation = useMutation({
    mutationFn: (id: number) => registrationApi.approve(id),
    onSuccess: () => {
      toast.success('Đã duyệt đăng ký');
      queryClient.invalidateQueries({ queryKey: queryKeys.registrations.all });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => registrationApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Đã từ chối đăng ký');
      queryClient.invalidateQueries({ queryKey: queryKeys.registrations.all });
      setRejectDialog({ open: false, id: null });
      setRejectReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const registrations = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<RegistrationResponse>[] = [
    { key: 'raceName', header: 'Cuộc đua', render: (r) => <span className="font-medium">{r.raceName}</span> },
    { key: 'horseName', header: 'Ngựa' },
    { key: 'horseCode', header: 'Mã ngựa' },
    { key: 'jockeyName', header: 'Nài' },
    { key: 'ownerName', header: 'Chủ sở hữu' },
    { key: 'registeredAt', header: 'Ngày ĐK', render: (r) => formatDate(r.registeredAt) },
    {
      key: 'status', header: 'Trạng thái',
      render: (r) => <StatusBadge status={r.status} colorClass={getRegistrationStatusColor(r.status)} />,
    },
    {
      key: 'actions', header: 'Thao tác',
      render: (r) => r.status === RegistrationStatus.PENDING ? (
        <div className="flex items-center gap-1">
          <Button size="sm" variant="ghost" className="text-green-600" onClick={() => approveMutation.mutate(r.id)}>
            <Check className="h-4 w-4" />
          </Button>
          <Button size="sm" variant="ghost" className="text-red-600" onClick={() => setRejectDialog({ open: true, id: r.id })}>
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : null,
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Quản lý Đăng ký</h1>

      <DataTable columns={columns} data={registrations ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={rejectDialog.open} onOpenChange={(open) => setRejectDialog({ open, id: open ? rejectDialog.id : null })}>
        <DialogContent>
          <DialogHeader><DialogTitle>Từ chối đăng ký</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <div>
              <Label>Lý do từ chối *</Label>
              <Input value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Nhập lý do..." />
            </div>
            <Button
              className="w-full bg-red-600 hover:bg-red-700 text-white"
              disabled={!rejectReason.trim()}
              onClick={() => rejectDialog.id && rejectMutation.mutate({ id: rejectDialog.id, reason: rejectReason })}
            >
              Từ chối
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default RegistrationManagementPage;