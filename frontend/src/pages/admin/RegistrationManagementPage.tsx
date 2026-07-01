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
import { Check, X, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const RegistrationManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [viewing, setViewing] = useState<RegistrationResponse | null>(null);
  const [rejecting, setRejecting] = useState(false);
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
      setRejecting(false);
      setRejectReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const registrations = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const openDetail = (r: RegistrationResponse) => {
    setViewing(r);
    setRejecting(false);
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

      <Dialog open={!!viewing} onOpenChange={(open) => { if (!open) { setViewing(null); setRejecting(false); setRejectReason(''); } }}>
        <DialogContent>
          <DialogHeader><DialogTitle>Thông tin đăng ký thi đấu</DialogTitle></DialogHeader>

          {viewing && !rejecting && (
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

              {viewing.status === RegistrationStatus.REJECTED && viewing.rejectReason && (
                <div>
                  <p className="text-sm text-muted-foreground">Lý do từ chối</p>
                  <p className="font-medium text-red-600">{viewing.rejectReason}</p>
                </div>
              )}

              {viewing.status === RegistrationStatus.PENDING && (
                <div className="flex justify-end gap-2 pt-2">
                  <Button variant="outline" className="text-red-600" onClick={() => setRejecting(true)}>
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
            </div>
          )}

          {viewing && rejecting && (
            <div className="space-y-4">
              <div>
                <Label>Lý do không duyệt *</Label>
                <Textarea
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  placeholder="Nhập lý do từ chối đăng ký này..."
                  rows={4}
                />
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setRejecting(false)}>Hủy</Button>
                <Button
                  className="bg-red-600 hover:bg-red-700 text-white"
                  disabled={!rejectReason.trim() || rejectMutation.isPending}
                  onClick={() => rejectMutation.mutate({ id: viewing.id, reason: rejectReason })}
                >
                  {rejectMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Xác nhận không duyệt
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default RegistrationManagementPage;
