import { z } from 'zod';

export const seasonSchema = z.object({
  name: z.string().min(1, 'Tên mùa giải bắt buộc'),
  startDate: z.string().min(1, 'Ngày bắt đầu bắt buộc'),
  endDate: z.string().min(1, 'Ngày kết thúc bắt buộc'),
  description: z.string().optional(),
}).refine((data) => new Date(data.endDate) > new Date(data.startDate), {
  message: 'Ngày kết thúc phải sau ngày bắt đầu',
  path: ['endDate'],
});

export type SeasonFormData = z.infer<typeof seasonSchema>;