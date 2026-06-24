import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { HorseStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getHorseStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { ArrowLeft, Edit, Loader2 } from 'lucide-react';

const HorseDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.detail(id ?? ''),
    queryFn: () => horseApi.getById(id ?? ''),
    enabled: !!id,
  });

  const horse = data?.data?.data;

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
        {horse.status === HorseStatus.PENDING && (
          <Link to={`/my-horses/${horse.id}/edit`}>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Edit className="h-4 w-4 mr-2" />Chỉnh sửa
            </Button>
          </Link>
        )}
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
    </div>
  );
};

export default HorseDetailPage;