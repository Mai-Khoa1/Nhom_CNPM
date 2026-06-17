import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Role } from '@/types/enums';

const GuestGuard = ({ children }: { children?: React.ReactNode }) => {
  const { isAuthenticated, role } = useAuthStore();
  if (isAuthenticated) {
    const redirect = role === Role.ADMIN || role === Role.ORGANIZER
      ? '/admin/dashboard' : '/my-horses';
    return <Navigate to={redirect} replace />;
  }
  return children ? <>{children}</> : <Outlet />;
};

export default GuestGuard;