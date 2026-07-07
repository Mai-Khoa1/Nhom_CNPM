import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { updateRequestApi } from '@/api/updateRequestApi';
import { queryKeys } from '@/constants/queryKeys';
import { UpdateRequestResponse } from '@/types/updateRequest';
import { UpdateRequestStatus, UpdateTargetType } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { formatDateTime } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Check, X, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const REQUEST_STATUS_LABELS: Record<UpdateRequestStatus, string> = {
  [UpdateRequestStatus.PENDING]: 'Chờ duyệt',
  [UpdateRequestStatus.APPROVED]: 'Đã duyệt',
  [UpdateRequestStatus.REJECTED]: 'Từ chối',
};

const getRequestStatusColor = (status: UpdateRequestStatus) => {
  switch (status) {
    case UpdateRequestStatus.PENDING: return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    case UpdateRequestStatus.APPROVED: return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case UpdateRequestStatus.REJECTED: return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    default: return 'bg-gray-100 text-gray-800';
  }
};

const formatValue = (v: unknown) => (v === null || v === undefined || v === '' ? '-' : String(v));

/**
 * FileRequestManagementPage - duyệt yêu cầu sửa/xóa tệp tin đã gắn với 1 đăng ký thi đấu đã APPROVED.
 * Không còn "tệp mới tải lên chờ duyệt" - tệp tin đính kèm khi đăng ký được duyệt cùng cả bộ hồ sơ
 * đăng ký (xem RegistrationManagementPage), không duyệt riêng lẻ theo file nữa.
 */
const FileRequestManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [viewing, setViewing] = useState<UpdateRequestResponse | null>(null);
  const [rejecting, setRejecting] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  const params = { page, size: 10, status: UpdateRequestStatus.PENDING, targetType: UpdateTargetType.FILE };
  const { data, isLoading } = useQuery({
    queryKey: queryKeys.updateRequests.list(params),
    queryFn: () => updateRequestApi.getAll(params),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey: queryKeys.updateRequests.all });

  const approveMutation = useMutation({
    mutationFn: (id: string) => updateRequestApi.approve(id),
    onSuccess: () => { toast.success('Đã duyệt yêu cầu'); invalidate(); setViewing(null); },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => updateRequestApi.reject(id, reason),
    onSuccess: () => { toast.success('Đã từ chối yêu cầu'); invalidate(); setViewing(null); setRejecting(false); setRejectReason(''); },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const requests = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const isDeleteRequest = (r: UpdateRequestResponse) => r.action === 'XOA';

  const columns: Column<UpdateRequestResponse>[] = [
    { key: 'targetName', header: 'Tệp tin', render: (r) => <span className="font-medium">{r.targetName}</span> },
    { key: 'action', header: 'Loại yêu cầu', render: (r) => (isDeleteRequest(r) ? 'Xóa' : 'Cập nhật') },
    { key: 'ownerName', header: 'Chủ ngựa' },
    { key: 'createdAt', header: 'Ngày gửi', render: (r) => formatDateTime(r.createdAt) },
    { key: 'status', header: 'Trạng thái', render: (r) => <StatusBadge status={REQUEST_STATUS_LABELS[r.status]} colorClass={getRequestStatusColor(r.status)} /> },
  ];

  const diffKeys = viewing
    ? Array.from(new Set([...Object.keys(viewing.oldData), ...Object.keys(viewing.newData)]))
    : [];

  const FIELD_LABELS: Record<string, string> = { tenFile: 'Tên file', loaiFile: 'Loại tệp', targetType: 'Đối tượng', targetId: 'Mã đối tượng' };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Yêu cầu cập nhật/xóa tệp tin</h1>

      <DataTable columns={columns} data={requests ?? []} isLoading={isLoading} emptyMessage="Không có yêu cầu tệp tin nào" onRowClick={setViewing} />
      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={!!viewing} onOpenChange={(open) => { if (!open) { setViewing(null); setRejecting(false); setRejectReason(''); } }}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              Yêu cầu {viewing && isDeleteRequest(viewing) ? 'xóa' : 'cập nhật'} tệp tin: {viewing?.targetName}
            </DialogTitle>
          </DialogHeader>

          {viewing && !rejecting && (
            <div className="space-y-4">
              <p className="text-sm text-muted-foreground">Chủ ngựa: <span className="font-medium text-foreground">{viewing.ownerName}</span></p>

              {isDeleteRequest(viewing) ? (
                <p className="text-sm text-red-600">Chủ ngựa yêu cầu xóa vĩnh viễn tệp tin này.</p>
              ) : (
                <div className="rounded-md border overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-muted">
                      <tr>
                        <th className="text-left p-2 font-medium">Trường thông tin</th>
                        <th className="text-left p-2 font-medium">Thông tin cũ</th>
                        <th className="text-left p-2 font-medium">Thông tin mới</th>
                      </tr>
                    </thead>
                    <tbody>
                      {diffKeys.map((key) => {
                        const oldVal = formatValue(viewing.oldData[key]);
                        const newVal = formatValue(viewing.newData[key]);
                        const changed = oldVal !== newVal;
                        return (
                          <tr key={key} className="border-t">
                            <td className="p-2 text-muted-foreground">{FIELD_LABELS[key] ?? key}</td>
                            <td className="p-2">{oldVal}</td>
                            <td className={changed ? 'p-2 font-medium text-[#D4A017]' : 'p-2'}>{newVal}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}

              <div className="flex justify-end gap-2 pt-2">
                <Button variant="outline" className="text-red-600" onClick={() => setRejecting(true)}>
                  <X className="h-4 w-4 mr-2" />Không duyệt
                </Button>
                <Button className="bg-green-600 hover:bg-green-700 text-white" disabled={approveMutation.isPending} onClick={() => approveMutation.mutate(viewing.id)}>
                  {approveMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  <Check className="h-4 w-4 mr-2" />Duyệt
                </Button>
              </div>
            </div>
          )}

          {viewing && rejecting && (
            <div className="space-y-4">
              <div>
                <Label>Lý do không duyệt *</Label>
                <Textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Nhập lý do từ chối..." rows={4} />
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

export default FileRequestManagementPage;
