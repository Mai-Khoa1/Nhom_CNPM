import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { RegistrationResponse } from '@/types/registration';
import { RegistrationStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRegistrationStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, XCircle, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const MyRegistrationsPage = () => {
  const [page, setPage] = useState(0);
  const [cancelTarget, setCancelTarget] = useState<RegistrationResponse | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const params = { page, size: 10 };
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.registrations.my(params),
    queryFn: () => registrationApi.getMy(params),
  });

  const registrations = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => registrationApi.cancel(id, reason),
    onSuccess: () => {
      toast.success('Đã hủy đăng ký');
      queryClient.invalidateQueries({ queryKey: queryKeys.registrations.all });
      setCancelTarget(null);
      setCancelReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const canCancel = (r: RegistrationResponse) =>
    r.status === RegistrationStatus.PENDING || r.status === RegistrationStatus.APPROVED;

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
    {
      key: 'actions', header: '',
      render: (r) => canCancel(r) ? (
        <Button variant="ghost" size="sm" className="text-red-600 hover:text-red-700" onClick={() => setCancelTarget(r)}>
          <XCircle className="h-4 w-4 mr-1" />Hủy đăng ký
        </Button>
      ) : null,
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

      <Dialog open={!!cancelTarget} onOpenChange={(open) => { if (!open) { setCancelTarget(null); setCancelReason(''); } }}>
        <DialogContent>
          <DialogHeader><DialogTitle>Hủy đăng ký thi đấu</DialogTitle></DialogHeader>
          <div className="space-y-4">
            <p className="text-sm text-muted-foreground">
              Hủy đăng ký ngựa "{cancelTarget?.horseName}" tại cuộc đua "{cancelTarget?.raceName}". Hành động này không thể hoàn tác.
            </p>
            <div>
              <Label>Lý do hủy *</Label>
              <Textarea
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                placeholder="Nhập lý do hủy đăng ký..."
                rows={4}
              />
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => { setCancelTarget(null); setCancelReason(''); }}>Đóng</Button>
              <Button
                className="bg-red-600 hover:bg-red-700 text-white"
                disabled={!cancelReason.trim() || cancelMutation.isPending}
                onClick={() => cancelTarget && cancelMutation.mutate({ id: cancelTarget.id, reason: cancelReason })}
              >
                {cancelMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Xác nhận hủy
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default MyRegistrationsPage;