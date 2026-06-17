import { cn } from '@/utils/cn';

interface StatusBadgeProps {
  status: string;
  colorClass: string;
}

export const StatusBadge = ({ status, colorClass }: StatusBadgeProps) => {
  return (
    <span className={cn('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', colorClass)}>
      {status}
    </span>
  );
};