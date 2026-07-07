import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { uploadApi } from '@/api/uploadApi';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { FileUploadResponse } from '@/types/upload';
import { Button } from '@/components/ui/button';
import { PaginationControl } from '@/components/common/PaginationControl';
import { DataTable, Column } from '@/components/common/DataTable';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { formatDateTime } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, Edit, Trash2, Download } from 'lucide-react';
import { toast } from 'sonner';

const MyFilesPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [deleteTarget, setDeleteTarget] = useState<FileUploadResponse | null>(null);

  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.files.list(params),
    queryFn: () => uploadApi.list(params),
  });

  const { data: registrationsData } = useQuery({
    queryKey: queryKeys.registrations.my({ size: 200 }),
    queryFn: () => registrationApi.getMy({ size: 200 }),
  });

  const registrationLabelById = new Map(
    (registrationsData?.data?.data?.content ?? []).map((r) => [r.id, `${r.horseName} - ${r.jockeyName} (${r.raceName})`])
  );

  const resolveTargetName = (f: FileUploadResponse) => {
    if (!f.targetType || !f.targetId) return '-';
    return registrationLabelById.get(f.targetId) ?? f.targetId;
  };

  const deleteMutation = useMutation({
    mutationFn: (id: string) => uploadApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa tệp tin (hoặc đã gửi yêu cầu xóa nếu đăng ký liên quan đã được duyệt)');
      queryClient.invalidateQueries({ queryKey: queryKeys.files.all });
      setDeleteTarget(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const handleDownload = async (f: FileUploadResponse) => {
    try {
      const res = await uploadApi.getById(f.fileId);
      const url = URL.createObjectURL(res.data as Blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = f.fileName;
      a.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      toast.error(handleApiError(error));
    }
  };

  const files: FileUploadResponse[] = (data?.data?.data?.content ?? []).map((f) => ({ ...f, id: f.fileId } as FileUploadResponse & { id: string }));
  const pageInfo = data?.data?.data;

  const columns: Column<FileUploadResponse>[] = [
    { key: 'fileName', header: 'Tên file', render: (f) => <span className="font-medium">{f.fileName}</span> },
    { key: 'fileCategory', header: 'Loại tệp' },
    { key: 'target', header: 'Đăng ký thi đấu', render: resolveTargetName },
    { key: 'fileSize', header: 'Kích thước', render: (f) => `${(f.fileSize / 1024).toFixed(1)} KB` },
    { key: 'createdAt', header: 'Ngày tải lên', render: (f) => formatDateTime(f.createdAt) },
    {
      key: 'actions', header: 'Thao tác',
      render: (f) => (
        <div className="flex items-center gap-1">
          <Button variant="ghost" size="icon" onClick={() => handleDownload(f)}>
            <Download className="h-4 w-4" />
          </Button>
          <Link to={`/my-files/${f.fileId}/edit`}>
            <Button variant="ghost" size="icon"><Edit className="h-4 w-4" /></Button>
          </Link>
          <Button variant="ghost" size="icon" onClick={() => setDeleteTarget(f)}>
            <Trash2 className="h-4 w-4 text-red-500" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Tệp tin của tôi</h1>
        <Link to="/my-files/create">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
            <Plus className="h-4 w-4 mr-2" />Thêm tệp tin
          </Button>
        </Link>
      </div>

      <DataTable columns={columns} data={files} isLoading={isLoading} emptyMessage="Bạn chưa có tệp tin nào" />

      {pageInfo && (
        <PaginationControl
          page={pageInfo.page}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={setPage}
        />
      )}

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        title="Xóa tệp tin"
        description={`Xóa tệp "${deleteTarget?.fileName}"? Nếu đăng ký liên quan đã được duyệt, yêu cầu xóa sẽ được gửi tới Ban tổ chức để duyệt trước khi xóa thật.`}
        onConfirm={() => deleteTarget && deleteMutation.mutate(deleteTarget.fileId)}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default MyFilesPage;
