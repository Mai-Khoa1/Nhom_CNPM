import { z } from 'zod';
import { Gender } from '@/types/enums';

export const horseSchema = z.object({
  name: z.string().min(1, 'Tên ngựa bắt buộc').max(100, 'Tối đa 100 ký tự'),
  code: z.string().min(1, 'Mã ngựa bắt buộc').max(20, 'Tối đa 20 ký tự'),
  breed: z.string().min(1, 'Giống ngựa bắt buộc').max(100, 'Tối đa 100 ký tự'),
  dateOfBirth: z.string().refine((val) => {
    const date = new Date(val);
    return date < new Date();
  }, 'Ngày sinh phải là ngày trong quá khứ'),
  gender: z.nativeEnum(Gender, { errorMap: () => ({ message: 'Chọn giới tính' }) }),
  color: z.string().optional(),
  weight: z.number().min(100, 'Tối thiểu 100 kg').max(1200, 'Tối đa 1200 kg').optional(),
});

export type HorseFormData = z.infer<typeof horseSchema>;