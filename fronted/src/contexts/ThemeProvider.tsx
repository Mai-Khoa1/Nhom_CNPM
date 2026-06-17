import { useEffect } from 'react';
import { useUiStore } from '@/store/uiStore';

export const ThemeProvider = ({ children }: { children: React.ReactNode }) => {
  const { theme } = useUiStore();

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle('dark', theme === 'dark');
  }, [theme]);

  return <>{children}</>;
};