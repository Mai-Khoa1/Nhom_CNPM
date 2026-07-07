import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { loginSchema, LoginFormData } from '@/schemas/loginSchema';
import { authApi } from '@/api/authApi';
import { useAuthStore } from '@/store/authStore';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { toast } from 'sonner';
import { Loader2 } from 'lucide-react';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const mutation = useMutation({
    mutationFn: (data: LoginFormData) => authApi.login(data),
    onSuccess: (response) => {
      const { accessToken, refreshToken, user } = response.data.data;
      login(user, accessToken, refreshToken);
      toast.success('Đăng nhập thành công!');
      let redirect = '/';
      if (user.role === 'ADMIN' || user.role === 'ORGANIZER') redirect = '/admin/dashboard';
      else if (user.role === 'HORSE_OWNER') redirect = '/my-horses';
      navigate(redirect);
    },
    onError: (error) => {
      toast.error(handleApiError(error));
    },
  });

  const onSubmit = (data: LoginFormData) => {
    mutation.mutate(data);
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-center mb-6 text-[#1A2B4A] dark:text-white">Đăng nhập</h2>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <Label htmlFor="username">Tên đăng nhập</Label>
          <Input id="username" {...register('username')} placeholder="Nhập tên đăng nhập" />
          {errors.username && <p className="text-sm text-red-500 mt-1">{errors.username.message}</p>}
        </div>
        <div>
          <Label htmlFor="password">Mật khẩu</Label>
          <Input id="password" type="password" {...register('password')} placeholder="Nhập mật khẩu" />
          {errors.password && <p className="text-sm text-red-500 mt-1">{errors.password.message}</p>}
        </div>
        <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
          {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          Đăng nhập
        </Button>
      </form>
      <p className="text-center text-sm mt-4 text-muted-foreground">
        Chưa có tài khoản?{' '}
        <Link to="/register" className="text-[#D4A017] hover:underline font-medium">Đăng ký ngay</Link>
      </p>
    </div>
  );
};

export default LoginPage;