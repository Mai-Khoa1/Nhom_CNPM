import { z } from 'zod';
import { Role } from '@/types/enums';

export const userCreateSchema = z.object({
  username: z.string().min(3, 'Tối thiểu 3 ký tự').max(50),
  password: z.string().min(6, 'Mật khẩu tối thiểu 6 ký tự'),
  fullName: z.string().min(1, 'Họ tên bắt buộc'),
  email: z.string().email('Email không hợp lệ'),
  phone: z.string().optional(),
  role: z.nativeEnum(Role),
});

export type UserCreateFormData = z.infer<typeof userCreateSchema>;

export const userUpdateSchema = z.object({
  fullName: z.string().min(1, 'Họ tên bắt buộc'),
  email: z.string().email('Email không hợp lệ'),
  phone: z.string().optional(),
});

export type UserUpdateFormData = z.infer<typeof userUpdateSchema>;
