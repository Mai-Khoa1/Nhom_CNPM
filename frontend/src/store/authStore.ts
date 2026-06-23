import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { UserResponse } from '@/types/auth';
import { Role } from '@/types/enums';
import { tokenService } from '@/services/tokenService';

interface AuthState {
  user: UserResponse | null;
  role: Role | null;
  accessToken: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  login: (user: UserResponse, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  setUser: (user: UserResponse) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      role: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      login: (user, accessToken, refreshToken) => {
        tokenService.saveTokens(accessToken, refreshToken);
        set({
          user,
          role: user.role,
          accessToken,
          refreshToken,
          isAuthenticated: true,
        });
      },

      logout: () => {
        tokenService.clearTokens();
        set({
          user: null,
          role: null,
          accessToken: null,
          refreshToken: null,
          isAuthenticated: false,
        });
        window.location.href = '/login';
      },

      setUser: (user) => set({ user, role: user.role }),
    }),
    { name: 'auth-store' }
  )
);