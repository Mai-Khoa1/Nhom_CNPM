import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from '@/components/ui/sonner';
import { ThemeProvider } from '@/contexts/ThemeProvider';
import { Role } from '@/types/enums';

// Layouts
import MainLayout from '@/layouts/MainLayout';
import AuthLayout from '@/layouts/AuthLayout';
import AdminLayout from '@/layouts/AdminLayout';
import DashboardLayout from '@/layouts/DashboardLayout';

// Guards
import AuthGuard from '@/routes/AuthGuard';
import GuestGuard from '@/routes/GuestGuard';
import RoleGuard from '@/routes/RoleGuard';

// Public Pages
import HomePage from '@/pages/public/HomePage';
import LoginPage from '@/pages/public/LoginPage';
import RegisterPage from '@/pages/public/RegisterPage';
import RaceListPage from '@/pages/public/RaceListPage';
import RaceDetailPage from '@/pages/public/RaceDetailPage';
import LeaderboardPage from '@/pages/public/LeaderboardPage';

// Horse Owner Pages
import MyHorsesPage from '@/pages/horse-owner/MyHorsesPage';
import HorseCreatePage from '@/pages/horse-owner/HorseCreatePage';
import HorseDetailPage from '@/pages/horse-owner/HorseDetailPage';
import HorseEditPage from '@/pages/horse-owner/HorseEditPage';
import MyJockeysPage from '@/pages/horse-owner/MyJockeysPage';
import JockeyCreatePage from '@/pages/horse-owner/JockeyCreatePage';
import JockeyDetailPage from '@/pages/horse-owner/JockeyDetailPage';
import JockeyEditPage from '@/pages/horse-owner/JockeyEditPage';
import MyRegistrationsPage from '@/pages/horse-owner/MyRegistrationsPage';
import RegistrationCreatePage from '@/pages/horse-owner/RegistrationCreatePage';
import RegistrationDetailPage from '@/pages/horse-owner/RegistrationDetailPage';
import NotificationsPage from '@/pages/horse-owner/NotificationsPage';
import ProfilePage from '@/pages/horse-owner/ProfilePage';

// Admin Pages
import DashboardPage from '@/pages/admin/DashboardPage';
import SeasonManagementPage from '@/pages/admin/SeasonManagementPage';
import RaceManagementPage from '@/pages/admin/RaceManagementPage';
import LaneManagementPage from '@/pages/admin/LaneManagementPage';
import HorseManagementPage from '@/pages/admin/HorseManagementPage';
import JockeyManagementPage from '@/pages/admin/JockeyManagementPage';
import UpdateRequestManagementPage from '@/pages/admin/UpdateRequestManagementPage';
import RegistrationManagementPage from '@/pages/admin/RegistrationManagementPage';
import ResultManagementPage from '@/pages/admin/ResultManagementPage';
import AdminNotificationsPage from '@/pages/admin/AdminNotificationsPage';
import FileManagementPage from '@/pages/admin/FileManagementPage';
import UserManagementPage from '@/pages/admin/UserManagementPage';
import RoleManagementPage from '@/pages/admin/RoleManagementPage';
import AuditLogPage from '@/pages/admin/AuditLogPage';
import SystemPage from '@/pages/admin/SystemPage';

// Error Pages
import ForbiddenPage from '@/pages/ForbiddenPage';
import NotFoundPage from '@/pages/NotFoundPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
      staleTime: 30000,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <BrowserRouter>
          <Routes>
            {/* Auth Routes (Guest only) */}
            <Route element={<GuestGuard><AuthLayout /></GuestGuard>}>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
            </Route>

            {/* Public Routes */}
            <Route element={<MainLayout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/races" element={<RaceListPage />} />
              <Route path="/races/:id" element={<RaceDetailPage />} />
              <Route path="/leaderboard" element={<LeaderboardPage />} />
            </Route>

            {/* Horse Owner Routes */}
            <Route element={<AuthGuard><RoleGuard allowedRoles={[Role.HORSE_OWNER]}><DashboardLayout /></RoleGuard></AuthGuard>}>
              <Route path="/my-horses" element={<MyHorsesPage />} />
              <Route path="/my-horses/create" element={<HorseCreatePage />} />
              <Route path="/my-horses/:id" element={<HorseDetailPage />} />
              <Route path="/my-horses/:id/edit" element={<HorseEditPage />} />
              <Route path="/my-jockeys" element={<MyJockeysPage />} />
              <Route path="/my-jockeys/create" element={<JockeyCreatePage />} />
              <Route path="/my-jockeys/:id" element={<JockeyDetailPage />} />
              <Route path="/my-jockeys/:id/edit" element={<JockeyEditPage />} />
              <Route path="/my-registrations" element={<MyRegistrationsPage />} />
              <Route path="/my-registrations/new" element={<RegistrationCreatePage />} />
              <Route path="/my-registrations/:id" element={<RegistrationDetailPage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>

            {/* Organizer + Admin Routes */}
            <Route element={<AuthGuard><RoleGuard allowedRoles={[Role.ORGANIZER, Role.ADMIN]}><AdminLayout /></RoleGuard></AuthGuard>}>
              <Route path="/admin/dashboard" element={<DashboardPage />} />
              <Route path="/admin/seasons" element={<SeasonManagementPage />} />
              <Route path="/admin/races" element={<RaceManagementPage />} />
              <Route path="/admin/lanes" element={<LaneManagementPage />} />
              <Route path="/admin/horses" element={<HorseManagementPage />} />
              <Route path="/admin/jockeys" element={<JockeyManagementPage />} />
              <Route path="/admin/update-requests" element={<UpdateRequestManagementPage />} />
              <Route path="/admin/registrations" element={<RegistrationManagementPage />} />
              <Route path="/admin/results" element={<ResultManagementPage />} />
              <Route path="/admin/notifications" element={<AdminNotificationsPage />} />
              <Route path="/admin/files" element={<FileManagementPage />} />
            </Route>

            {/* Admin Only Routes */}
            <Route element={<AuthGuard><RoleGuard allowedRoles={[Role.ADMIN]}><AdminLayout /></RoleGuard></AuthGuard>}>
              <Route path="/admin/users" element={<UserManagementPage />} />
              <Route path="/admin/roles" element={<RoleManagementPage />} />
              <Route path="/admin/audit-logs" element={<AuditLogPage />} />
              <Route path="/admin/system" element={<SystemPage />} />
            </Route>

            {/* Error Pages */}
            <Route path="/403" element={<ForbiddenPage />} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </BrowserRouter>
        <Toaster position="top-right" richColors />
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;