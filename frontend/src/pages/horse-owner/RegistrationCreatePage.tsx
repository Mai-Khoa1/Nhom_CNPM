import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { horseApi } from '@/api/horseApi';
import { jockeyApi } from '@/api/jockeyApi';
import { raceApi } from '@/api/raceApi';
import { queryKeys } from '@/constants/queryKeys';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { formatDateTime } from '@/utils/formatDate';
import { toast } from 'sonner';
import { Loader2, ArrowLeft } from 'lucide-react';

const registrationSchema = z.object({
  raceId: z.string({ required_error: 'Chọn cuộc đua' }).min(1, 'Chọn cuộc đua'),
  horseId: z.string({ required_error: 'Chọn ngựa' }).min(1, 'Chọn ngựa'),
  jockeyId: z.string({ required_error: 'Chọn nài' }).min(1, 'Chọn nài'),
});

type RegistrationFormData = z.infer<typeof registrationSchema>;

const RegistrationCreatePage = () => {
  const navigate = useNavigate();

  const { handleSubmit, setValue, formState: { errors } } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema),
  });

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ status: 'OPEN', size: 100 }),
    queryFn: () => raceApi.getAll({ status: 'OPEN', size: 100 }),
  });

  const { data: horsesData } = useQuery({
    queryKey: queryKeys.horses.list({ status: 'APPROVED', size: 100 }),
    queryFn: () => horseApi.getAll({ status: 'APPROVED', size: 100 }),
  });

  const { data: jockeysData } = useQuery({
    queryKey: queryKeys.jockeys.list({ size: 100 }),
    queryFn: () => jockeyApi.getAll({ size: 100 }),
  });

  const races = racesData?.data?.data?.content ?? [];
  const horses = horsesData?.data?.data?.content ?? [];
  const jockeys = jockeysData?.data?.data?.content ?? [];

  const mutation = useMutation({
    mutationFn: (data: RegistrationFormData) => registrationApi.create(data),
    onSuccess: () => {
      toast.success('Đăng ký thi đấu thành công! Đang chờ duyệt.');
      navigate('/my-registrations');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const onSubmit = (data: RegistrationFormData) => mutation.mutate(data);

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-registrations')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Đăng ký thi đấu mới</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-2xl">
            <div>
              <Label>Cuộc đua *</Label>
              <Select onValueChange={(v) => setValue('raceId', v)}>
                <SelectTrigger><SelectValue placeholder="Chọn cuộc đua" /></SelectTrigger>
                <SelectContent>
                  {races.map((race) => (
                    <SelectItem key={race.id} value={race.id}>
                      {race.name} — {race.location} ({race.raceDate ? formatDateTime(race.raceDate) : '?'})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.raceId && <p className="text-sm text-red-500 mt-1">{errors.raceId.message}</p>}
            </div>
            <div>
              <Label>Ngựa *</Label>
              <Select onValueChange={(v) => setValue('horseId', v)}>
                <SelectTrigger><SelectValue placeholder="Chọn ngựa" /></SelectTrigger>
                <SelectContent>
                  {horses.map((horse) => (
                    <SelectItem key={horse.id} value={horse.id}>
                      {horse.name} ({horse.code})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.horseId && <p className="text-sm text-red-500 mt-1">{errors.horseId.message}</p>}
            </div>
            <div>
              <Label>Nài *</Label>
              <Select onValueChange={(v) => setValue('jockeyId', v)}>
                <SelectTrigger><SelectValue placeholder="Chọn nài" /></SelectTrigger>
                <SelectContent>
                  {jockeys.map((jockey) => (
                    <SelectItem key={jockey.id} value={jockey.id}>
                      {jockey.fullName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.jockeyId && <p className="text-sm text-red-500 mt-1">{errors.jockeyId.message}</p>}
            </div>
            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Đăng ký
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-registrations')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default RegistrationCreatePage;