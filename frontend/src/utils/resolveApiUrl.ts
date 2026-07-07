import { ENV } from '@/config/env';

/**
 * BE trả các URL ảnh/tài liệu (avatarUrl, passportUrl...) dưới dạng đường dẫn tương đối
 * (ví dụ "/horses/N001/avatar") để không phụ thuộc host/port của FE. Nếu dùng thẳng trong
 * <img src> mà không qua axiosInstance, trình duyệt sẽ resolve theo origin của FE (sai) -
 * hàm này ghép lại đúng origin của API trước khi gắn vào src/href.
 */
export const resolveApiUrl = (path?: string | null): string | undefined => {
  if (!path) return undefined;
  if (/^https?:\/\//i.test(path)) return path;
  return `${ENV.API_URL}${path}`;
};
