import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Shield, Users, Crown, Eye } from 'lucide-react';

const RoleManagementPage = () => {
  const roles = [
    {
      name: 'ADMIN',
      label: 'Quản trị viên',
      icon: Crown,
      color: 'text-red-600 bg-red-50',
      permissions: [
        'Quản lý toàn bộ hệ thống',
        'Quản lý người dùng & phân quyền',
        'Xem audit log',
        'Cấu hình hệ thống',
        'Tất cả quyền của Organizer',
      ],
    },
    {
      name: 'ORGANIZER',
      label: 'Ban tổ chức',
      icon: Shield,
      color: 'text-blue-600 bg-blue-50',
      permissions: [
        'Quản lý mùa giải & cuộc đua',
        'Duyệt/từ chối ngựa & nài',
        'Duyệt/từ chối đăng ký thi đấu',
        'Phân làn đua',
        'Nhập & công bố kết quả',
        'Quản lý tệp tin',
      ],
    },
    {
      name: 'HORSE_OWNER',
      label: 'Chủ ngựa',
      icon: Users,
      color: 'text-green-600 bg-green-50',
      permissions: [
        'Quản lý ngựa của mình',
        'Quản lý nài của mình',
        'Đăng ký thi đấu',
        'Xem kết quả & bảng xếp hạng',
        'Nhận thông báo',
      ],
    },
    {
      name: 'SPECTATOR',
      label: 'Khán giả',
      icon: Eye,
      color: 'text-gray-600 bg-gray-50',
      permissions: [
        'Xem danh sách cuộc đua',
        'Xem chi tiết cuộc đua',
        'Xem bảng xếp hạng',
        'Xem kết quả công bố',
      ],
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Phân quyền hệ thống</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {roles.map((role) => {
          const Icon = role.icon;
          return (
            <Card key={role.name}>
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className={`h-10 w-10 rounded-lg flex items-center justify-center ${role.color}`}>
                    <Icon className="h-5 w-5" />
                  </div>
                  <div>
                    <CardTitle className="text-base">{role.label}</CardTitle>
                    <p className="text-xs text-muted-foreground font-mono">{role.name}</p>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2">
                  {role.permissions.map((perm, idx) => (
                    <li key={idx} className="flex items-center gap-2 text-sm">
                      <span className="h-1.5 w-1.5 rounded-full bg-[#D4A017]" />
                      {perm}
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
};

export default RoleManagementPage;