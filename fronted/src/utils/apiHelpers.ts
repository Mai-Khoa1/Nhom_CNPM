import axios from 'axios';

export const handleApiError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status;
    const message = (error.response?.data as { message?: string })?.message;
    switch (status) {
      case 400: return message ?? 'Dữ liệu không hợp lệ';
      case 401: return 'Phiên đăng nhập hết hạn';
      case 403: return 'Không có quyền thực hiện';
      case 404: return 'Không tìm thấy dữ liệu';
      case 409: return message ?? 'Dữ liệu đã tồn tại';
      case 500: return 'Lỗi máy chủ, vui lòng thử lại';
      default: return message ?? 'Đã xảy ra lỗi';
    }
  }
  return 'Đã xảy ra lỗi không xác định';
};