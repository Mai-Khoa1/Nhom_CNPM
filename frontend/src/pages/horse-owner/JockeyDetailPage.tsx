import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { JockeyStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getJockeyStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { ArrowLeft, Edit, Loader2 } from 'lucide-react';

const JockeyDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.jockeys.detail(Number(id)),
    queryFn: () => jockeyApi.getById(Number(id)),
    enabled: !!id,
  });

  const jockey = data?.data?.data;

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
        {jockey.status === JockeyStatus.PENDING && (
          <Link to={`/my-jockeys/${jockey.id}/edit`}>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Edit className="h-4 w-4 mr-2" />Chỉnh sửa
            </Button>
          </Link>
        )}
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl">{jockey.fullName}</CardTitle>
            <StatusBadge status={jockey.status} colorClass={getJockeyStatusColor(jockey.status)} />
          </div>
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
        </CardContent>
      </Card>
    </div>
  );
};

export default JockeyDetailPage;