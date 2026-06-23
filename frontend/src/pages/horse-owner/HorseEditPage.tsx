import { useParams, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { horseSchema, HorseFormData } from '@/schemas/horseSchema';
import { horseApi } from '@/api/horseApi';
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

const HorseEditPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isLoading: loadingHorse } = useQuery({
    queryKey: queryKeys.horses.detail(id ?? ''),
    queryFn: () => horseApi.getById(id ?? ''),
    enabled: !!id,
  });

  const horse = data?.data?.data;

  const { register, handleSubmit, setValue, formState: { errors } } = useForm<HorseFormData>({
    resolver: zodResolver(horseSchema),
    values: horse ? {
      name: horse.name,
      code: horse.code,
      breed: horse.breed,
      dateOfBirth: horse.dateOfBirth,
      gender: horse.gender,
      color: horse.color || '',
      weight: horse.weight,
    } : undefined,
  });

  const mutation = useMutation({
    mutationFn: (formData: HorseFormData) => horseApi.update(id ?? '', formData),
    onSuccess: () => {
      toast.success('Cập nhật ngựa thành công!');
      queryClient.invalidateQueries({ queryKey: queryKeys.horses.all });
      navigate('/my-horses');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const onSubmit = (formData: HorseFormData) => mutation.mutate(formData);

  if (loadingHorse) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
      </div>
    );
  }

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-horses')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Chỉnh sửa ngựa</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-2xl">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="name">Tên ngựa *</Label>
                <Input id="name" {...register('name')} />
                {errors.name && <p className="text-sm text-red-500 mt-1">{errors.name.message}</p>}
              </div>
              <div>
                <Label htmlFor="code">Mã ngựa *</Label>
                <Input id="code" {...register('code')} disabled />
                {errors.code && <p className="text-sm text-red-500 mt-1">{errors.code.message}</p>}
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="breed">Giống ngựa *</Label>
                <Input id="breed" {...register('breed')} />
                {errors.breed && <p className="text-sm text-red-500 mt-1">{errors.breed.message}</p>}
              </div>
              <div>
                <Label>Giới tính *</Label>
                <Select value={horse?.gender} onValueChange={(v) => setValue('gender', v as Gender)}>
                  <SelectTrigger><SelectValue placeholder="Chọn giới tính" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value={Gender.MALE}>Đực (MALE)</SelectItem>
                    <SelectItem value={Gender.FEMALE}>Cái (FEMALE)</SelectItem>
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
                <Label htmlFor="weight">Cân nặng (kg)</Label>
                <Input id="weight" type="number" {...register('weight', { valueAsNumber: true })} placeholder="100-1200" />
                {errors.weight && <p className="text-sm text-red-500 mt-1">{errors.weight.message}</p>}
              </div>
            </div>
            <div>
              <Label htmlFor="color">Màu lông</Label>
              <Input id="color" {...register('color')} placeholder="Nâu, Đen, Trắng..." />
            </div>
            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Lưu thay đổi
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-horses')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default HorseEditPage;