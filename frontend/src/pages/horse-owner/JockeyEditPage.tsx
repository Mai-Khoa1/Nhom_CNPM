import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { jockeySchema, JockeyFormData } from '@/schemas/jockeySchema';
import { jockeyApi } from '@/api/jockeyApi';
import { queryKeys } from '@/constants/queryKeys';
import { Gender } from '@/types/enums';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';
import { Loader2, ArrowLeft } from 'lucide-react';

const JockeyEditPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isLoading: loadingJockey } = useQuery({
    queryKey: queryKeys.jockeys.detail(id ?? ''),
    queryFn: () => jockeyApi.getById(id ?? ''),
    enabled: !!id,
  });

  const jockey = data?.data?.data;

  const { register, handleSubmit, setValue, formState: { errors } } = useForm<JockeyFormData>({
    resolver: zodResolver(jockeySchema),
    values: jockey ? {
      fullName: jockey.fullName,
      dateOfBirth: jockey.dateOfBirth,
      gender: jockey.gender,
      experienceYears: jockey.experienceYears,
      weight: jockey.weight,
      licenseNumber: jockey.licenseNumber || '',
    } : undefined,
  });

  const mutation = useMutation({
    mutationFn: (formData: JockeyFormData) => jockeyApi.update(id ?? '', formData),
    onSuccess: () => {
      toast.success('Cập nhật nài thành công!');
      queryClient.invalidateQueries({ queryKey: queryKeys.jockeys.all });
      navigate('/my-jockeys');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const onSubmit = (formData: JockeyFormData) => mutation.mutate(formData);

  if (loadingJockey) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
      </div>
    );
  }

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-jockeys')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Chỉnh sửa nài</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-2xl">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="fullName">Họ tên *</Label>
                <Input id="fullName" {...register('fullName')} />
                {errors.fullName && <p className="text-sm text-red-500 mt-1">{errors.fullName.message}</p>}
              </div>
              <div>
                <Label>Giới tính *</Label>
                <Select value={jockey?.gender} onValueChange={(v) => setValue('gender', v as Gender)}>
                  <SelectTrigger><SelectValue placeholder="Chọn giới tính" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value={Gender.MALE}>Nam</SelectItem>
                    <SelectItem value={Gender.FEMALE}>Nữ</SelectItem>
                  </SelectContent>
                </Select>
                {errors.gender && <p className="text-sm text-red-500 mt-1">{errors.gender.message}</p>}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="dateOfBirth">Ngày sinh *</Label>
                <Input id="dateOfBirth" type="date" {...register('dateOfBirth')} />
                {errors.dateOfBirth && <p className="text-sm text-red-500 mt-1">{errors.dateOfBirth.message}</p>}
              </div>
              <div>
                <Label htmlFor="experienceYears">Kinh nghiệm (năm)</Label>
                <Input id="experienceYears" type="number" {...register('experienceYears', { valueAsNumber: true })} />
                {errors.experienceYears && <p className="text-sm text-red-500 mt-1">{errors.experienceYears.message}</p>}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="weight">Cân nặng (kg)</Label>
                <Input id="weight" type="number" {...register('weight', { valueAsNumber: true })} />
                {errors.weight && <p className="text-sm text-red-500 mt-1">{errors.weight.message}</p>}
              </div>
              <div>
                <Label htmlFor="licenseNumber">Số giấy phép</Label>
                <Input id="licenseNumber" {...register('licenseNumber')} />
              </div>
            </div>
            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Lưu thay đổi
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-jockeys')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default JockeyEditPage;