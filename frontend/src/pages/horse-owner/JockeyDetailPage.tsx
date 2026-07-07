import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { uploadApi } from '@/api/uploadApi';
import { queryKeys } from '@/constants/queryKeys';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { resolveApiUrl } from '@/utils/resolveApiUrl';
import { ArrowLeft, Edit, Trash2, Loader2, FileText } from 'lucide-react';
import { toast } from 'sonner';

const JockeyDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.jockeys.detail(id ?? ''),
    queryFn: () => jockeyApi.getById(id ?? ''),
    enabled: !!id,
  });

  const jockey = data?.data?.data;

  const deleteMutation = useMutation({
    mutationFn: () => jockeyApi.delete(id ?? ''),
    onSuccess: () => {
      toast.success('Đã xóa nài');
      queryClient.invalidateQueries({ queryKey: queryKeys.jockeys.all });
      navigate('/my-jockeys');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  /** licenseScanUrl/medicalCertUrl trỏ tới /upload/{id} (cần Bearer token) nên không thể dùng thẳng
   * <a href> - phải tải blob qua axiosInstance rồi mở bằng URL tạm, khác avatarUrl là endpoint công khai. */
  const openProtectedFile = async (fileUrl: string, fileLabel: string) => {
    const fileId = fileUrl.split('/').pop();
    if (!fileId) return;
    try {
      const res = await uploadApi.getById(fileId);
      const url = URL.createObjectURL(res.data as Blob);
      window.open(url, '_blank');
    } catch (error) {
      toast.error(handleApiError(error) || `Không thể mở ${fileLabel}`);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
      </div>
    );
  }

  if (!jockey) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin nài</p>
        <Button variant="ghost" onClick={() => navigate('/my-jockeys')} className="mt-4">
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <Button variant="ghost" onClick={() => navigate('/my-jockeys')}>
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
        <div className="flex gap-2">
          <Link to={`/my-jockeys/${jockey.id}/edit`}>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Edit className="h-4 w-4 mr-2" />Chỉnh sửa
            </Button>
          </Link>
          <Button variant="destructive" onClick={() => setShowDeleteDialog(true)}>
            <Trash2 className="h-4 w-4 mr-2" />Xóa
          </Button>
        </div>
      </div>

      <Card>
        {jockey.avatarUrl && (
          <div className="aspect-video bg-muted overflow-hidden rounded-t-xl">
            <img src={resolveApiUrl(jockey.avatarUrl)} alt={jockey.fullName} className="h-full w-full object-cover" />
          </div>
        )}
        <CardHeader>
          <CardTitle className="text-xl">{jockey.fullName}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Giới tính</p>
                <p className="font-medium">{jockey.gender === 'MALE' ? 'Nam' : 'Nữ'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngày sinh</p>
                <p className="font-medium">{formatDate(jockey.dateOfBirth)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Kinh nghiệm</p>
                <p className="font-medium">{jockey.experienceYears ? `${jockey.experienceYears} năm` : '-'}</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Cân nặng</p>
                <p className="font-medium">{jockey.weight ? `${jockey.weight} kg` : '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Số giấy phép</p>
                <p className="font-medium">{jockey.licenseNumber || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Chủ sở hữu</p>
                <p className="font-medium">{jockey.ownerName}</p>
              </div>
            </div>
          </div>

          {(jockey.licenseScanUrl || jockey.medicalCertUrl) && (
            <div className="flex flex-wrap gap-2 mt-6 pt-4 border-t">
              {jockey.licenseScanUrl && (
                <Button variant="outline" size="sm" onClick={() => openProtectedFile(jockey.licenseScanUrl!, 'giấy phép nài')}>
                  <FileText className="h-4 w-4 mr-2" />Xem giấy phép nài
                </Button>
              )}
              {jockey.medicalCertUrl && (
                <Button variant="outline" size="sm" onClick={() => openProtectedFile(jockey.medicalCertUrl!, 'giấy khám sức khỏe')}>
                  <FileText className="h-4 w-4 mr-2" />Xem giấy khám sức khỏe
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      <ConfirmDialog
        open={showDeleteDialog}
        onOpenChange={setShowDeleteDialog}
        title="Xóa nài"
        description="Bạn có chắc muốn xóa nài này? Hành động không thể hoàn tác."
        onConfirm={() => deleteMutation.mutate()}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default JockeyDetailPage;