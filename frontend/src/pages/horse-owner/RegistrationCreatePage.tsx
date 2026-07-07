import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery, useMutation } from '@tanstack/react-query';
import { registrationApi } from '@/api/registrationApi';
import { horseApi } from '@/api/horseApi';
import { jockeyApi } from '@/api/jockeyApi';
import { raceApi } from '@/api/raceApi';
import { organizerApi } from '@/api/organizerApi';
import { uploadApi } from '@/api/uploadApi';
import { queryKeys } from '@/constants/queryKeys';
import { FileType } from '@/types/enums';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { formatDateTime } from '@/utils/formatDate';
import { toast } from 'sonner';
import { Loader2, ArrowLeft, Upload } from 'lucide-react';

const registrationSchema = z.object({
  organizerId: z.string({ required_error: 'Chọn Ban tổ chức' }).min(1, 'Chọn Ban tổ chức'),
  raceId: z.string({ required_error: 'Chọn cuộc đua' }).min(1, 'Chọn cuộc đua'),
  horseId: z.string({ required_error: 'Chọn ngựa' }).min(1, 'Chọn ngựa'),
  jockeyId: z.string({ required_error: 'Chọn nài' }).min(1, 'Chọn nài'),
});

type RegistrationFormData = z.infer<typeof registrationSchema>;

/** Các loại file có thể đính kèm ngay khi đăng ký thi đấu (mục 5.2) - đều không bắt buộc. */
const ATTACHMENT_SLOTS: { key: string; label: string; fileType: FileType }[] = [
  { key: 'horsePhoto', label: 'Ảnh ngựa', fileType: FileType.HORSE_PHOTO },
  { key: 'jockeyAvatar', label: 'Ảnh nài', fileType: FileType.JOCKEY_AVATAR },
  { key: 'healthCert', label: 'Hồ sơ sức khỏe ngựa', fileType: FileType.HEALTH_CERTIFICATE },
  { key: 'licenseScan', label: 'Giấy phép/chứng chỉ nài', fileType: FileType.LICENSE_SCAN },
];

const RegistrationCreatePage = () => {
  const navigate = useNavigate();
  const [organizerId, setOrganizerId] = useState('');
  const [attachments, setAttachments] = useState<Record<string, File | null>>({});

  const { handleSubmit, setValue, formState: { errors } } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema),
  });

  const { data: organizersData } = useQuery({
    queryKey: queryKeys.organizers.all,
    queryFn: () => organizerApi.getAll(),
  });

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ status: 'OPEN', organizerId, size: 100 }),
    queryFn: () => raceApi.getAll({ status: 'OPEN', organizerId, size: 100 }),
    enabled: !!organizerId,
  });

  const { data: horsesData } = useQuery({
    queryKey: queryKeys.horses.list({ size: 100 }),
    queryFn: () => horseApi.getAll({ size: 100 }),
    enabled: !!organizerId,
  });

  const { data: jockeysData } = useQuery({
    queryKey: queryKeys.jockeys.list({ size: 100 }),
    queryFn: () => jockeyApi.getAll({ size: 100 }),
    enabled: !!organizerId,
  });

  const organizers = organizersData?.data?.data ?? [];
  const races = racesData?.data?.data?.content ?? [];
  const horses = horsesData?.data?.data?.content ?? [];
  const jockeys = jockeysData?.data?.data?.content ?? [];

  const mutation = useMutation({
    mutationFn: async (data: RegistrationFormData) => {
      const { data: created } = await registrationApi.create({
        raceId: data.raceId,
        horseId: data.horseId,
        jockeyId: data.jockeyId,
      });
      const registrationId = created.data?.id;

      if (registrationId) {
        const uploads = ATTACHMENT_SLOTS
          .filter((slot) => attachments[slot.key])
          .map((slot) => uploadApi.upload({
            file: attachments[slot.key]!,
            targetType: 'DANG_KY',
            targetId: registrationId,
            fileType: slot.fileType,
          }));
        await Promise.all(uploads);
      }
      return created;
    },
    onSuccess: () => {
      toast.success('Đăng ký thi đấu thành công! Đang chờ duyệt.');
      navigate('/my-registrations');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const onSubmit = (data: RegistrationFormData) => mutation.mutate(data);

  const handleOrganizerChange = (value: string) => {
    setOrganizerId(value);
    setValue('organizerId', value);
    // Đổi Ban tổ chức thì các lựa chọn cuộc đua/ngựa/nài trước đó không còn hợp lệ chéo BTC.
    setValue('raceId', '');
  };

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-registrations')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Đăng ký thi đấu mới</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 max-w-2xl">
            <div>
              <Label>Ban tổ chức *</Label>
              <Select onValueChange={handleOrganizerChange}>
                <SelectTrigger><SelectValue placeholder="Chọn Ban tổ chức nhận đăng ký" /></SelectTrigger>
                <SelectContent>
                  {organizers.map((org) => (
                    <SelectItem key={org.id} value={org.id}>{org.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.organizerId && <p className="text-sm text-red-500 mt-1">{errors.organizerId.message}</p>}
              <p className="text-xs text-muted-foreground mt-1">Chọn Ban tổ chức trước để xem các cuộc đua đang mở đăng ký của họ.</p>
            </div>

            <div>
              <Label>Cuộc đua *</Label>
              <Select disabled={!organizerId} onValueChange={(v) => setValue('raceId', v)}>
                <SelectTrigger><SelectValue placeholder={organizerId ? 'Chọn cuộc đua' : 'Chọn Ban tổ chức trước'} /></SelectTrigger>
                <SelectContent>
                  {races.map((race) => (
                    <SelectItem key={race.id} value={race.id}>
                      {race.name} — {race.location} ({race.raceDate ? formatDateTime(race.raceDate) : '?'})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.raceId && <p className="text-sm text-red-500 mt-1">{errors.raceId.message}</p>}
              {organizerId && races.length === 0 && (
                <p className="text-xs text-muted-foreground mt-1">Ban tổ chức này chưa có cuộc đua nào đang mở đăng ký.</p>
              )}
            </div>

            <div>
              <Label>Ngựa *</Label>
              <Select disabled={!organizerId} onValueChange={(v) => setValue('horseId', v)}>
                <SelectTrigger><SelectValue placeholder="Chọn ngựa" /></SelectTrigger>
                <SelectContent>
                  {horses.map((horse) => (
                    <SelectItem key={horse.id} value={horse.id}>
                      {horse.name} ({horse.code})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.horseId && <p className="text-sm text-red-500 mt-1">{errors.horseId.message}</p>}
            </div>

            <div>
              <Label>Nài *</Label>
              <Select disabled={!organizerId} onValueChange={(v) => setValue('jockeyId', v)}>
                <SelectTrigger><SelectValue placeholder="Chọn nài" /></SelectTrigger>
                <SelectContent>
                  {jockeys.map((jockey) => (
                    <SelectItem key={jockey.id} value={jockey.id}>
                      {jockey.fullName}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.jockeyId && <p className="text-sm text-red-500 mt-1">{errors.jockeyId.message}</p>}
            </div>

            <div className="pt-2 border-t">
              <Label className="text-base">Tệp đính kèm (không bắt buộc)</Label>
              <p className="text-xs text-muted-foreground mb-3">
                Ảnh ngựa/nài, hồ sơ sức khỏe, giấy phép... gắn riêng cho lần đăng ký này.
              </p>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {ATTACHMENT_SLOTS.map((slot) => (
                  <div key={slot.key}>
                    <Label htmlFor={slot.key} className="text-sm font-normal flex items-center gap-2 cursor-pointer">
                      <Upload className="h-4 w-4" />
                      {attachments[slot.key]?.name ?? slot.label}
                    </Label>
                    <input
                      id={slot.key}
                      type="file"
                      accept="image/jpeg,image/png,image/gif,image/webp,application/pdf"
                      className="hidden"
                      onChange={(e) => setAttachments((prev) => ({ ...prev, [slot.key]: e.target.files?.[0] ?? null }))}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      className="w-full mt-1 justify-start truncate"
                      onClick={() => document.getElementById(slot.key)?.click()}
                    >
                      {attachments[slot.key]?.name ?? `Chọn file cho: ${slot.label}`}
                    </Button>
                  </div>
                ))}
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Đăng ký
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-registrations')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default RegistrationCreatePage;
