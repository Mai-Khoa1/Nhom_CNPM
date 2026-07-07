import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { formatDate } from '@/utils/formatDate';
import { resolveApiUrl } from '@/utils/resolveApiUrl';
import { ArrowLeft } from 'lucide-react';

const JockeyDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.jockeys.detail(id ?? ''),
    queryFn: () => jockeyApi.getById(id ?? ''),
    enabled: !!id,
  });

  const jockey = data?.data?.data;

  if (isLoading) {
    return <LoadingSpinner className="py-24" text="Đang tải thông tin nài ngựa..." />;
  }

  if (!jockey) {
    return (
      <div className="container mx-auto px-4 py-12 text-center">
        <p className="text-muted-foreground">Không tìm thấy thông tin nài ngựa</p>
        <Button variant="ghost" onClick={() => navigate('/jockeys')} className="mt-4">
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-3xl">
      <Button variant="ghost" onClick={() => navigate('/jockeys')} className="mb-6">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại danh sách nài ngựa
      </Button>

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
                <p className="font-medium">{jockey.experienceYears ?? 0} năm</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Cân nặng</p>
                <p className="font-medium">{jockey.weight ? `${jockey.weight} kg` : '-'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Chủ ngựa</p>
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
