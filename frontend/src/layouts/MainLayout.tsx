import { Outlet, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { useUiStore } from '@/store/uiStore';
import { Button } from '@/components/ui/button';
import { Sun, Moon, User, LogOut } from 'lucide-react';
import { Role } from '@/types/enums';

const MainLayout = () => {
  const { isAuthenticated, user, role, logout } = useAuthStore();
  const { theme, toggleTheme } = useUiStore();
  const location = useLocation();

  const navLinks = [
    { path: '/', label: 'Trang chủ' },
    { path: '/races', label: 'Cuộc đua' },
    { path: '/leaderboard', label: 'Bảng xếp hạng' },
  ];

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-50 border-b bg-white/80 dark:bg-gray-900/80 backdrop-blur-sm">
        <div className="container mx-auto flex h-16 items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center gap-2">
              <span className="text-2xl">🏇</span>
              <span className="text-xl font-bold text-[#1A2B4A] dark:text-[#D4A017]">Horse Racing</span>
            </Link>
            <nav className="hidden md:flex items-center gap-6">
              {navLinks.map((link) => (
                <Link
                  key={link.path}
                  to={link.path}
                  className={`text-sm font-medium transition-colors hover:text-[#D4A017] ${
                    location.pathname === link.path ? 'text-[#D4A017]' : 'text-muted-foreground'
                  }`}
                >
                  {link.label}
                </Link>
              ))}
            </nav>
          </div>
          <div className="flex items-center gap-3">
            <Button variant="ghost" size="icon" onClick={toggleTheme}>
              {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                <Link to={role === Role.ADMIN || role === Role.ORGANIZER ? '/admin/dashboard' : '/my-horses'}>
                  <Button variant="outline" size="sm">
                    <User className="h-4 w-4 mr-1" />
                    {user?.fullName}
                  </Button>
                </Link>
                <Button variant="ghost" size="icon" onClick={logout}>
                  <LogOut className="h-4 w-4" />
                </Button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login">
                  <Button variant="outline" size="sm">Đăng nhập</Button>
                </Link>
                <Link to="/register">
                  <Button size="sm" className="bg-[#D4A017] hover:bg-[#C8940A] text-white">Đăng ký</Button>
                </Link>
              </div>
            )}
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
      <footer className="border-t bg-[#1A2B4A] text-white py-8 mt-12">
        <div className="container mx-auto px-4 text-center">
          <p className="text-sm text-gray-300">© 2024 Horse Racing Tournament. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;