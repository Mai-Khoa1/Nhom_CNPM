import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { HorseStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/common/StatusBadge';
import { ConfirmDialog } from '@/components/common/ConfirmDialog';
import { getHorseStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { ArrowLeft, Edit, Trash2, Loader2 } from 'lucide-react';
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

  const canEdit = horse.status !== HorseStatus.DISQUALIFIED;
  const canDelete = horse.status === HorseStatus.PENDING || horse.status === HorseStatus.REJECTED;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <Button variant="ghost" onClick={() => navigate('/my-horses')}>
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
        <div className="flex gap-2">
          {canEdit && (
            <Link to={`/my-horses/${horse.id}/edit`}>
              <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
                <Edit className="h-4 w-4 mr-2" />
                {horse.status === HorseStatus.APPROVED ? 'Yêu cầu cập nhật' : 'Chỉnh sửa'}
              </Button>
            </Link>
          )}
          {canDelete && (
            <Button variant="destructive" onClick={() => setShowDeleteDialog(true)}>
              <Trash2 className="h-4 w-4 mr-2" />Xóa
            </Button>
          )}
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl">{horse.name}</CardTitle>
            <StatusBadge status={horse.status} colorClass={getHorseStatusColor(horse.status)} />
          </div>
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