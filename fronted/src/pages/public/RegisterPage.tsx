import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { registerSchema, RegisterFormData } from '@/schemas/registerSchema';
import { authApi } from '@/api/authApi';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';

const RegisterPage = () => {
  const navigate = useNavigate();

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const mutation = useMutation({
    mutationFn: (data: RegisterFormData) => authApi.register({
      username: data.username,
      password: data.password,
      fullName: data.fullName,
      email: data.email,
      phone: data.phone,
    }),
    onSuccess: () => {
      toast.success('Đăng ký thành công! Vui lòng đăng nhập.');
      navigate('/login');
    },
    onError: (error) => {
      toast.error(handleApiError(error));
    },
  });

  const onSubmit = (data: RegisterFormData) => {
    mutation.mutate(data);
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-center mb-6 text-[#1A2B4A] dark:text-white">Đăng ký</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <Label htmlFor="username">Tên đăng nhập</Label>
          <Input id="username" {...register('username')} placeholder="Tối thiểu 3 ký tự" />
          {errors.username && <p className="text-sm text-red-500 mt-1">{errors.username.message}</p>}
        </div>
        <div>
          <Label htmlFor="fullName">Họ và tên</Label>
          <Input id="fullName" {...register('fullName')} placeholder="Nhập họ tên đầy đủ" />
          {errors.fullName && <p className="text-sm text-red-500 mt-1">{errors.fullName.message}</p>}
        </div>
        <div>
          <Label htmlFor="email">Email</Label>
          <Input id="email" type="email" {...register('email')} placeholder="example@email.com" />
          {errors.email && <p className="text-sm text-red-500 mt-1">{errors.email.message}</p>}
        </div>
        <div>
          <Label htmlFor="phone">Số điện thoại (tùy chọn)</Label>
          <Input id="phone" {...register('phone')} placeholder="0901234567" />
        </div>
        <div>
          <Label htmlFor="password">Mật khẩu</Label>
          <Input id="password" type="password" {...register('password')} placeholder="Tối thiểu 6 ký tự" />
          {errors.password && <p className="text-sm text-red-500 mt-1">{errors.password.message}</p>}
        </div>
        <div>
          <Label htmlFor="confirmPassword">Xác nhận mật khẩu</Label>
          <Input id="confirmPassword" type="password" {...register('confirmPassword')} placeholder="Nhập lại mật khẩu" />
          {errors.confirmPassword && <p className="text-sm text-red-500 mt-1">{errors.confirmPassword.message}</p>}
        </div>
        <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
          {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Đăng ký
        </Button>
      </form>
      <p className="text-center text-sm mt-4 text-muted-foreground">
        Đã có tài khoản?{' '}
        <Link to="/login" className="text-[#D4A017] hover:underline font-medium">Đăng nhập</Link>
      </p>
    </div>
  );
};

export default RegisterPage;