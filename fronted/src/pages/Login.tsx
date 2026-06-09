import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Trophy } from 'lucide-react';
import { useAppDispatch, setCredentials } from '@/store';
import { authAPI } from '@/lib/api';
import { toast } from 'sonner';

export default function Login() {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [loginForm, setLoginForm] = useState({
    username: '',
    password: '',
  });
  const [registerForm, setRegisterForm] = useState({ username: '', email: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginForm.email || !loginForm.password) {
      toast.error('Vui lòng nhập đầy đủ thông tin');
      return;
    }
    setLoading(true);
    try {
      const response = await authAPI.login(
        loginForm.email,
        loginForm.password
      );
      const { token, username, email, role } = response.data;
      dispatch(setCredentials({ token, username, email, role }));
      toast.success('Đăng nhập thành công!');
      navigate('/');
    } catch {
      toast.error('Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!registerForm.username || !registerForm.email || !registerForm.password) {
      toast.error('Vui lòng nhập đầy đủ thông tin');
      return;
    }
    if (registerForm.password !== registerForm.confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp');
      return;
    }
    setLoading(true);
    try {
      const response = await authAPI.register(registerForm.username, registerForm.email, registerForm.password);
      const { token, username, email, role } = response.data;
      dispatch(setCredentials({ token, username, email, role }));
      toast.success('Đăng ký thành công!');
      navigate('/');
    } catch {
      toast.error('Đăng ký thất bại. Username hoặc email đã tồn tại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-900 p-4">
      <Card className="w-full max-w-md bg-gray-800 border-gray-700">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-emerald-600">
              <Trophy className="h-8 w-8 text-white" />
            </div>
          </div>
          <CardTitle className="text-2xl text-white">Horse Racing Admin</CardTitle>
          <CardDescription className="text-gray-400">Hệ thống quản lý đua ngựa</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs defaultValue="login" className="w-full">
            <TabsList className="grid w-full grid-cols-2 bg-gray-700">
              <TabsTrigger value="login" className="data-[state=active]:bg-emerald-600 data-[state=active]:text-white">Đăng nhập</TabsTrigger>
              <TabsTrigger value="register" className="data-[state=active]:bg-emerald-600 data-[state=active]:text-white">Đăng ký</TabsTrigger>
            </TabsList>
            <TabsContent value="login">
              <form onSubmit={handleLogin} className="space-y-4 mt-4">
                <div className="space-y-2">
                  <Label htmlFor="login-email" className="text-gray-300">
                    Email
                  </Label>
                  <Input
                    id="login-email"
                    type="email"
                    value={loginForm.email}
                    onChange={(e) =>
                      setLoginForm({
                        ...loginForm,
                        email: e.target.value
                      })
                    }
                    placeholder="admin@equestria.vn"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="login-password" className="text-gray-300">Mật khẩu</Label>
                  <Input
                    id="login-password"
                    type="password"
                    value={loginForm.password}
                    onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                    placeholder="••••••••"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white" disabled={loading}>
                  {loading ? 'Đang xử lý...' : 'Đăng nhập'}
                </Button>
              </form>
            </TabsContent>
            <TabsContent value="register">
              <form onSubmit={handleRegister} className="space-y-4 mt-4">
                <div className="space-y-2">
                  <Label htmlFor="reg-username" className="text-gray-300">Tên đăng nhập</Label>
                  <Input
                    id="reg-username"
                    value={registerForm.username}
                    onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                    placeholder="username"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reg-email" className="text-gray-300">Email</Label>
                  <Input
                    id="reg-email"
                    type="email"
                    value={registerForm.email}
                    onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
                    placeholder="email@example.com"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reg-password" className="text-gray-300">Mật khẩu</Label>
                  <Input
                    id="reg-password"
                    type="password"
                    value={registerForm.password}
                    onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                    placeholder="••••••••"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="reg-confirm" className="text-gray-300">Xác nhận mật khẩu</Label>
                  <Input
                    id="reg-confirm"
                    type="password"
                    value={registerForm.confirmPassword}
                    onChange={(e) => setRegisterForm({ ...registerForm, confirmPassword: e.target.value })}
                    placeholder="••••••••"
                    className="bg-gray-700 border-gray-600 text-white placeholder:text-gray-500"
                  />
                </div>
                <Button type="submit" className="w-full bg-emerald-600 hover:bg-emerald-700 text-white" disabled={loading}>
                  {loading ? 'Đang xử lý...' : 'Đăng ký'}
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  );
}