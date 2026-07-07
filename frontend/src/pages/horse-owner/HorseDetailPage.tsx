import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
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

const HorseDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.detail(id ?? ''),
    queryFn: () => horseApi.getById(id ?? ''),
    enabled: !!id,
  });

  const horse = data?.data?.data;

  const deleteMutation = useMutation({
    mutationFn: () => horseApi.delete(id ?? ''),
    onSuccess: () => {
      toast.success('Đã xóa ngựa');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
      navigate('/my-horses');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  /** passportUrl/healthCertUrl trỏ tới /upload/{id} (cần Bearer token) nên không thể dùng thẳng <a href> -
   * phải tải blob qua axiosInstance rồi mở bằng URL tạm, khác avatarUrl là endpoint công khai. */
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

  if (!horse) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin ngựa</p>
        <Button variant="ghost" onClick={() => navigate('/my-horses')} className="mt-4">
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <Button variant="ghost" onClick={() => navigate('/my-horses')}>
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
        <div className="flex gap-2">
          <Link to={`/my-horses/${horse.id}/edit`}>
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
        {horse.avatarUrl && (
          <div className="aspect-video bg-muted overflow-hidden rounded-t-xl">
            <img src={resolveApiUrl(horse.avatarUrl)} alt={horse.name} className="h-full w-full object-cover" />
          </div>
        )}
        <CardHeader>
          <CardTitle className="text-xl">{horse.name}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Mã ngựa</p>
                <p className="font-medium">{horse.code}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Giống</p>
                <p className="font-medium">{horse.breed}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Giới tính</p>
                <p className="font-medium">{horse.gender === 'MALE' ? 'Đực' : 'Cái'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngày sinh</p>
                <p className="font-medium">{formatDate(horse.dateOfBirth)}</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Màu lông</p>
                <p className="font-medium">{horse.color || '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Cân nặng</p>
                <p className="font-medium">{horse.weight ? `${horse.weight} kg` : '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Chủ sở hữu</p>
                <p className="font-medium">{horse.ownerName}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngày tạo</p>
                <p className="font-medium">{formatDate(horse.createdAt)}</p>
              </div>
            </div>
          </div>

          {(horse.passportUrl || horse.healthCertUrl) && (
            <div className="flex flex-wrap gap-2 mt-6 pt-4 border-t">
              {horse.passportUrl && (
                <Button variant="outline" size="sm" onClick={() => openProtectedFile(horse.passportUrl!, 'hộ chiếu ngựa')}>
                  <FileText className="h-4 w-4 mr-2" />Xem hộ chiếu ngựa
                </Button>
              )}
              {horse.healthCertUrl && (
                <Button variant="outline" size="sm" onClick={() => openProtectedFile(horse.healthCertUrl!, 'giấy khám sức khỏe')}>
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
        title="Xóa ngựa"
        description="Bạn có chắc muốn xóa ngựa này? Hành động không thể hoàn tác."
        onConfirm={() => deleteMutation.mutate()}
        variant="destructive"
        confirmText="Xóa"
      />
    </div>
  );
};

export default HorseDetailPage;