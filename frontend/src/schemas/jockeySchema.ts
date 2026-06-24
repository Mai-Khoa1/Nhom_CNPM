import { z } from 'zod';
import { Gender } from '@/types/enums';

export const jockeySchema = z.object({
  fullName: z.string().min(1, 'Tên nài bắt buộc').max(100),
  dateOfBirth: z.string().refine((val) => {
    const age = new Date().getFullYear() - new Date(val).getFullYear();
    return age >= 18 && age <= 60;
  }, 'Tuổi nài phải từ 18 đến 60'),
  gender: z.nativeEnum(Gender, { errorMap: () => ({ message: 'Chọn giới tính' }) }),
  experienceYears: z.number().min(0).max(50).optional(),
  weight: z.number().min(30, 'Tối thiểu 30 kg').max(80, 'Tối đa 80 kg').optional(),
  licenseNumber: z.string().optional(),
});

export type JockeyFormData = z.infer<typeof jockeySchema>;