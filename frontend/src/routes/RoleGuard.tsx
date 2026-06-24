import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '@/store/authStore';
import { Role } from '@/types/enums';

interface RoleGuardProps {
  allowedRoles: Role[];
  children?: React.ReactNode;
}

const RoleGuard = ({ allowedRoles, children }: RoleGuardProps) => {
  const { role } = useAuthStore();
  if (!role || !allowedRoles.includes(role)) return <Navigate to="/403" replace />;
  return children ? <>{children}</> : <Outlet />;
};

export default RoleGuard;