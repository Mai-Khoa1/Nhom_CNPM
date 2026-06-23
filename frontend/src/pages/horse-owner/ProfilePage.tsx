import { useAuthStore } from '@/store/authStore';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { User, Mail, Phone, Shield, Calendar } from 'lucide-react';

const ProfilePage = () => {
  const { user } = useAuthStore();

  if (!user) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">Không tìm thấy thông tin người dùng</p>
      </div>
    );
  }

  const roleLabel = {
    ADMIN: 'Quản trị viên',
    ORGANIZER: 'Ban tổ chức',
    HORSE_OWNER: 'Chủ ngựa',
    SPECTATOR: 'Khán giả',
  }[user.role] || user.role;

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Hồ sơ cá nhân</h1>

      <div className="max-w-2xl">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-4">
              <div className="h-16 w-16 rounded-full bg-[#1A2B4A] flex items-center justify-center">
                <User className="h-8 w-8 text-[#D4A017]" />
              </div>
              <div>
                <CardTitle className="text-xl">{user.fullName}</CardTitle>
                <p className="text-sm text-muted-foreground">@{user.username}</p>
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
                <Mail className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">Email</p>
                  <p className="font-medium">{user.email}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
                <Phone className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">Số điện thoại</p>
                  <p className="font-medium">{user.phone || 'Chưa cập nhật'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
                <Shield className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">Vai trò</p>
                  <p className="font-medium">{roleLabel}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
                <Calendar className="h-5 w-5 text-muted-foreground" />
                <div>
                  <p className="text-sm text-muted-foreground">Trạng thái</p>
                  <p className="font-medium text-green-600">{user.status === 'ACTIVE' ? 'Hoạt động' : 'Bị khóa'}</p>
                </div>
              </div>
            </div>
            <div className="mt-6">
              <Button variant="outline" disabled>
                Chỉnh sửa hồ sơ (Sắp ra mắt)
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default ProfilePage;