import { z } from 'zod';

export const raceSchema = z.object({
  seasonId: z.string().min(1, 'Chọn mùa giải'),
  name: z.string().min(1, 'Tên cuộc đua bắt buộc'),
  raceDate: z.string().refine((val) => new Date(val) > new Date(), 'Ngày đua phải là ngày tương lai'),
  location: z.string().min(1, 'Địa điểm bắt buộc'),
  distance: z.number().min(100, 'Tối thiểu 100 mét').max(10000, 'Tối đa 10000 mét'),
  maxHorses: z.number().min(2, 'Tối thiểu 2 ngựa').max(30, 'Tối đa 30 ngựa'),
  minHorses: z.number().min(2, 'Tối thiểu 2 ngựa').max(30, 'Tối đa 30 ngựa').optional(),
  description: z.string().optional(),
}).refine((data) => !data.minHorses || data.minHorses <= data.maxHorses, {
  message: 'Số ngựa tối thiểu không được lớn hơn số ngựa tối đa',
  path: ['minHorses'],
});

export type RaceFormData = z.infer<typeof raceSchema>;