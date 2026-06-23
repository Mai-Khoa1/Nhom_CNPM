import dayjs from 'dayjs';

export const formatDate = (date: string, format = 'DD/MM/YYYY') => {
  return dayjs(date).format(format);
};

export const formatDateTime = (date: string) => {
  return dayjs(date).format('DD/MM/YYYY HH:mm');
};

export const formatRelative = (date: string) => {
  const diff = dayjs().diff(dayjs(date), 'minute');
  if (diff < 1) return 'Vừa xong';
  if (diff < 60) return `${diff} phút trước`;
  if (diff < 1440) return `${Math.floor(diff / 60)} giờ trước`;
  return dayjs(date).format('DD/MM/YYYY');
};