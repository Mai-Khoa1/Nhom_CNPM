import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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
import { formatDate, formatDateTime } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Check, X, Ban, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

type ReasonAction = 'REJECT' | 'DISQUALIFY' | null;

const RegistrationManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [viewing, setViewing] = useState<RegistrationResponse | null>(null);
  const [reasonAction, setReasonAction] = useState<ReasonAction>(null);
  const [rejectReason, setRejectReason] = useState('');

  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.registrations.list(params),
    queryFn: () => registrationApi.getAll(params),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: queryKeys.registrations.all });

  const approveMutation = useMutation({
    mutationFn: (id: string) => registrationApi.approve(id),
    onSuccess: () => {
      toast.success('Đã duyệt đăng ký');
      invalidate();
      setViewing(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => registrationApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Đã từ chối đăng ký');
      invalidate();
      setViewing(null);
      setReasonAction(null);
      setRejectReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const disqualifyMutation = useMutation({
    mutationFn: ({ id, reason, confirmRevokePublish }: { id: string; reason: string; confirmRevokePublish?: boolean }) =>
      registrationApi.disqualify(id, reason, confirmRevokePublish),
    onSuccess: () => {
      toast.success('Đã loại đăng ký');
      invalidate();
      setViewing(null);
      setReasonAction(null);
      setRejectReason('');
    },
    onError: (error, vars) => {
      // Lỗi 4: nếu đăng ký này đã có kết quả công bố, backend yêu cầu xác nhận thu hồi công bố trước.
      const message = handleApiError(error);
      if (message?.includes('Xác nhận rõ ràng') && !vars.confirmRevokePublish) {
        const confirmed = window.confirm(
          `${message}\n\nBấm OK để xác nhận thu hồi công bố và tiếp tục loại đăng ký này.`
        );
        if (confirmed) {
          disqualifyMutation.mutate({ ...vars, confirmRevokePublish: true });
          return;
        }
      }
      toast.error(message);
    },
  });

  const registrations = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const openDetail = (r: RegistrationResponse) => {
    setViewing(r);
    setReasonAction(null);
    setRejectReason('');
  };

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
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Quản lý Đăng ký</h1>

      <DataTable columns={columns} data={registrations ?? []} isLoading={isLoading} onRowClick={openDetail} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={!!viewing} onOpenChange={(open) => { if (!open) { setViewing(null); setReasonAction(null); setRejectReason(''); } }}>
        <DialogContent>
          <DialogHeader><DialogTitle>Thông tin đăng ký thi đấu</DialogTitle></DialogHeader>

          {viewing && !reasonAction && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-muted-foreground">Cuộc đua</p>
                  <p className="font-medium">{viewing.raceName}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngày đua</p>
                  <p className="font-medium">{formatDateTime(viewing.raceDate)}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngựa đăng ký</p>
                  <p className="font-medium">{viewing.horseName} ({viewing.horseCode})</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Nài</p>
                  <p className="font-medium">{viewing.jockeyName}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Chủ sở hữu</p>
                  <p className="font-medium">{viewing.ownerName}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Làn đua</p>
                  <p className="font-medium">{viewing.laneNumber ?? 'Chưa gán'}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngày đăng ký</p>
                  <p className="font-medium">{formatDateTime(viewing.registeredAt)}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Trạng thái</p>
                  <StatusBadge status={viewing.status} colorClass={getRegistrationStatusColor(viewing.status)} />
                </div>
              </div>

              {(viewing.status === RegistrationStatus.REJECTED
                || viewing.status === RegistrationStatus.CANCELLED
                || viewing.status === RegistrationStatus.DISQUALIFIED) && viewing.reason && (
                <div>
                  <p className="text-sm text-muted-foreground">
                    {viewing.status === RegistrationStatus.CANCELLED ? 'Lý do hủy' : 'Lý do'}
                  </p>
                  <p className="font-medium text-red-600">{viewing.reason}</p>
                </div>
              )}

              {viewing.status === RegistrationStatus.PENDING && (
                <div className="flex justify-end gap-2 pt-2">
                  <Button variant="outline" className="text-red-600" onClick={() => setReasonAction('REJECT')}>
                    <X className="h-4 w-4 mr-2" />Không duyệt
                  </Button>
                  <Button
                    className="bg-green-600 hover:bg-green-700 text-white"
                    disabled={approveMutation.isPending}
                    onClick={() => approveMutation.mutate(viewing.id)}
                  >
                    {approveMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    <Check className="h-4 w-4 mr-2" />Duyệt
                  </Button>
                </div>
              )}

              {viewing.status === RegistrationStatus.APPROVED && (
                <div className="flex justify-end gap-2 pt-2">
                  <Button variant="outline" className="text-purple-600" onClick={() => setReasonAction('DISQUALIFY')}>
                    <Ban className="h-4 w-4 mr-2" />Loại đăng ký
                  </Button>
                </div>
              )}
            </div>
          )}

          {viewing && reasonAction && (
            <div className="space-y-4">
              <div>
                <Label>{reasonAction === 'REJECT' ? 'Lý do không duyệt *' : 'Lý do loại *'}</Label>
                <Textarea
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  placeholder={reasonAction === 'REJECT' ? 'Nhập lý do từ chối đăng ký này...' : 'Nhập lý do loại đăng ký này...'}
                  rows={4}
                />
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setReasonAction(null)}>Hủy</Button>
                {reasonAction === 'REJECT' ? (
                  <Button
                    className="bg-red-600 hover:bg-red-700 text-white"
                    disabled={!rejectReason.trim() || rejectMutation.isPending}
                    onClick={() => rejectMutation.mutate({ id: viewing.id, reason: rejectReason })}
                  >
                    {rejectMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Xác nhận không duyệt
                  </Button>
                ) : (
                  <Button
                    className="bg-purple-600 hover:bg-purple-700 text-white"
                    disabled={!rejectReason.trim() || disqualifyMutation.isPending}
                    onClick={() => disqualifyMutation.mutate({ id: viewing.id, reason: rejectReason })}
                  >
                    {disqualifyMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                    Xác nhận loại
                  </Button>
                )}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default RegistrationManagementPage;
