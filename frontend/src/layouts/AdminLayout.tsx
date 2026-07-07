import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';
import { useNotificationStore } from '@/store/notificationStore';
import { useNotificationBadge } from '@/hooks/useNotificationBadge';
import { Button } from '@/components/ui/button';
import { Role } from '@/types/enums';
import {
  LayoutDashboard, Trophy, Flag, Layers, ClipboardList,
  Medal, Bell, FileText, Users, ScrollText, Settings,
  Sun, Moon, LogOut, Menu, ChevronLeft,
  ClipboardCheck,
} from 'lucide-react';
import { cn } from '@/utils/cn';

const AdminLayout = () => {
  const { user, role, logout } = useAuthStore();
  const { theme, toggleTheme, sidebarCollapsed, toggleSidebar } = useUiStore();
  const { unreadCount } = useNotificationStore();
  useNotificationBadge();
  const location = useLocation();

  // Ban tổ chức không còn quản lý danh sách Ngựa/Nài trực tiếp (hồ sơ do chủ ngựa tự quản lý, không cần
  // duyệt) - BTC chỉ xử lý qua từng LẦN ĐĂNG KÝ thi đấu gửi tới mình (mục "Đăng ký").
  const organizerLinks = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/admin/seasons', label: 'Mùa giải', icon: Trophy },
    { path: '/admin/races', label: 'Cuộc đua', icon: Flag },
    { path: '/admin/lanes', label: 'Làn đua', icon: Layers },
    { path: '/admin/update-requests', label: 'Duyệt cập nhật', icon: ClipboardCheck },
    { path: '/admin/registrations', label: 'Đăng ký', icon: ClipboardList },
    { path: '/admin/results', label: 'Kết quả', icon: Medal },
    { path: '/admin/notifications', label: 'Thông báo', icon: Bell },
    { path: '/admin/file-requests', label: 'Duyệt tệp tin', icon: FileText },
  ];

  // Admin chỉ còn đúng 4 mục: Dashboard, Người dùng, Audit Log, Hệ thống.
  // Toàn bộ nghiệp vụ đua (mùa giải/cuộc đua/ngựa/nài/đăng ký/kết quả/thông báo/tệp tin/phân quyền)
  // do Ban tổ chức (ORGANIZER) đảm nhiệm.
  const adminLinks = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/admin/users', label: 'Người dùng', icon: Users },
    { path: '/admin/audit-logs', label: 'Audit Log', icon: ScrollText },
    { path: '/admin/system', label: 'Hệ thống', icon: Settings },
  ];

  const links = role === Role.ADMIN ? adminLinks : organizerLinks;

  return (
    <div className="min-h-screen bg-background flex">
      {/* Sidebar */}
      <aside className={cn(
        'fixed inset-y-0 left-0 z-50 flex flex-col border-r bg-[#1A2B4A] text-white transition-all duration-300',
        sidebarCollapsed ? 'w-16' : 'w-64'
      )}>
        <div className="flex h-16 items-center justify-between px-4 border-b border-white/10">
          {!sidebarCollapsed && (
            <Link to="/admin/dashboard" className="flex items-center gap-2">
              <span className="text-xl">🏇</span>
              <span className="font-bold text-[#D4A017]">{role === Role.ADMIN ? 'Admin' : 'Ban tổ chức'}</span>
            </Link>
          )}
          <Button variant="ghost" size="icon" onClick={toggleSidebar} className="text-white hover:bg-white/10">
            {sidebarCollapsed ? <Menu className="h-5 w-5" /> : <ChevronLeft className="h-5 w-5" />}
          </Button>
        </div>
        <nav className="flex-1 overflow-y-auto py-4 px-2">
          {links.map((link) => {
            const Icon = link.icon;
            const isActive = location.pathname === link.path;
            return (
              <Link
                key={link.path}
                to={link.path}
                className={cn(
                  'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-colors mb-1',
                  isActive
                    ? 'bg-[#D4A017] text-white font-medium'
                    : 'text-gray-300 hover:bg-white/10 hover:text-white'
                )}
              >
                <Icon className="h-5 w-5 shrink-0" />
                {!sidebarCollapsed && <span>{link.label}</span>}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* Main Content */}
      <div className={cn('flex-1 flex flex-col transition-all duration-300', sidebarCollapsed ? 'ml-16' : 'ml-64')}>
        {/* Topbar */}
        <header className="sticky top-0 z-40 flex h-16 items-center justify-between border-b bg-white/80 dark:bg-gray-900/80 backdrop-blur-sm px-6">
          <Link to="/" className="text-sm text-muted-foreground hover:text-[#D4A017]">← Về trang chủ</Link>
          <div className="flex items-center gap-3">
            <Button variant="ghost" size="icon" onClick={toggleTheme}>
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            <Link to="/admin/notifications" className="relative">
              <Button variant="ghost" size="icon">
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 h-5 w-5 rounded-full bg-red-500 text-[10px] text-white flex items-center justify-center">
                    {unreadCount > 99 ? '99+' : unreadCount}
                  </span>
                )}
              </Button>
            </Link>
            <div className="flex items-center gap-2 pl-3 border-l">
              <span className="text-sm font-medium">{user?.fullName}</span>
              <Button variant="ghost" size="icon" onClick={logout}>
                <LogOut className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </header>
        {/* Page Content */}
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AdminLayout;