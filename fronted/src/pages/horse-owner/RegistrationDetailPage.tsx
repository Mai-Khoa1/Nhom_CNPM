import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getRegistrationStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { ArrowLeft, Loader2 } from 'lucide-react';

const RegistrationDetailPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.registrations.detail(Number(id)),
    queryFn: () => registrationApi.getById(Number(id)),
    enabled: !!id,
  });

  const registration = data?.data?.data;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
      </div>
    );
  }

  if (!registration) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin đăng ký</p>
        <Button variant="ghost" onClick={() => navigate('/my-registrations')} className="mt-4">
          <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
        </Button>
      </div>
    );
  }

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-registrations')} className="mb-6">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-xl">Chi tiết đăng ký #{registration.id}</CardTitle>
            <StatusBadge status={registration.status} colorClass={getRegistrationStatusColor(registration.status)} />
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Cuộc đua</p>
                <p className="font-medium">{registration.raceName}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngày đua</p>
                <p className="font-medium">{formatDate(registration.raceDate)}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngựa</p>
                <p className="font-medium">{registration.horseName} ({registration.horseCode})</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">Nài</p>
                <p className="font-medium">{registration.jockeyName}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Làn đua</p>
                <p className="font-medium">{registration.laneNumber ?? 'Chưa phân làn'}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Ngày đăng ký</p>
                <p className="font-medium">{formatDate(registration.registeredAt)}</p>
              </div>
              {registration.rejectReason && (
                <div>
                  <p className="text-sm text-muted-foreground">Lý do từ chối</p>
                  <p className="font-medium text-red-600">{registration.rejectReason}</p>
                </div>
              )}
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default RegistrationDetailPage;