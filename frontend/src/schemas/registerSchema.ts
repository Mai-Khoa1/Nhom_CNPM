import { z } from 'zod';
import { emailField } from './emailValidation';

export const registerSchema = z.object({
  username: z.string().min(3, 'Tối thiểu 3 ký tự').max(50),
  password: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
  confirmPassword: z.string(),
  fullName: z.string().min(1, 'Họ tên bắt buộc'),
  email: emailField,
  phone: z.string().optional(),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
});

export type RegisterFormData = z.infer<typeof registerSchema>;