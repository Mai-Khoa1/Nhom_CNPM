import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';

const AuthGuard = ({ children }: { children?: React.ReactNode }) => {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return children ? <>{children}</> : <Outlet />;
};

export default AuthGuard;