import { RaceStatus, RegistrationStatus, SeasonStatus } from '@/types/enums';

export const getRaceStatusColor = (status: RaceStatus) => {
  switch (status) {
    case RaceStatus.OPEN: return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case RaceStatus.CLOSED: return 'bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200';
    case RaceStatus.ONGOING: return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
    case RaceStatus.COMPLETED: return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
    case RaceStatus.CANCELLED: return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    default: return 'bg-gray-100 text-gray-800';
  }
};

export const getRegistrationStatusColor = (status: RegistrationStatus) => {
  switch (status) {
    case RegistrationStatus.PENDING: return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    case RegistrationStatus.APPROVED: return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case RegistrationStatus.REJECTED: return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    case RegistrationStatus.CANCELLED: return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    case RegistrationStatus.DISQUALIFIED: return 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200';
    default: return 'bg-gray-100 text-gray-800';
  }
};

export const getSeasonStatusColor = (status: SeasonStatus) => {
  switch (status) {
    case SeasonStatus.UPCOMING: return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
    case SeasonStatus.ONGOING: return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case SeasonStatus.CLOSED: return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    default: return 'bg-gray-100 text-gray-800';
  }
};