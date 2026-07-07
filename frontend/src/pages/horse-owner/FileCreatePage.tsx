import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { uploadApi } from '@/api/uploadApi';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { FileType } from '@/types/enums';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Upload, Loader2, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf'];

const FILE_TYPE_OPTIONS: { value: FileType; label: string }[] = [
  { value: FileType.HORSE_PHOTO, label: 'Ảnh ngựa' },
  { value: FileType.PASSPORT_SCAN, label: 'Hộ chiếu ngựa' },
  { value: FileType.HEALTH_CERTIFICATE, label: 'Giấy khám sức khỏe ngựa' },
  { value: FileType.JOCKEY_AVATAR, label: 'Ảnh nài' },
  { value: FileType.LICENSE_SCAN, label: 'Giấy phép nài' },
  { value: FileType.MEDICAL_CERTIFICATE, label: 'Giấy khám sức khỏe nài' },
];

const FileCreatePage = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [fileType, setFileType] = useState<FileType>(FileType.HORSE_PHOTO);
  const [registrationId, setRegistrationId] = useState<string>('');
  const [fileName, setFileName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  const { data: registrationsData } = useQuery({
    queryKey: queryKeys.registrations.my({ size: 200 }),
    queryFn: () => registrationApi.getMy({ size: 200 }),
  });

  const registrationOptions = (registrationsData?.data?.data?.content ?? []).map((r) => ({
    id: r.id,
    label: `${r.horseName} - ${r.jockeyName} (${r.raceName})`,
  }));

  const uploadMutation = useMutation({
    mutationFn: () => uploadApi.upload({
      file: selectedFile!,
      fileType,
      targetType: 'DANG_KY',
      targetId: registrationId || undefined,
      fileName: fileName || undefined,
    }),
    onSuccess: () => {
      toast.success('Tải tệp tin lên thành công!');
      queryClient.invalidateQueries({ queryKey: queryKeys.files.all });
      navigate('/my-files');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const handleFileChange = (file: File | undefined | null) => {
    if (!file) return;
    if (file.size > MAX_FILE_SIZE_BYTES) {
      toast.error('File vượt quá dung lượng tối đa cho phép (5MB)');
      return;
    }
    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      toast.error('Định dạng file không được hỗ trợ. Chỉ chấp nhận JPG, PNG, GIF, WEBP hoặc PDF.');
      return;
    }
    setSelectedFile(file);
    if (!fileName) setFileName(file.name);
  };

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      toast.error('Vui lòng chọn file để tải lên');
      return;
    }
    if (!registrationId) {
      toast.error('Vui lòng chọn lần đăng ký thi đấu để gắn tệp tin này vào');
      return;
    }
    uploadMutation.mutate();
  };

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-files')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Thêm tệp tin mới</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4 max-w-2xl">
            <div>
              <Label>Gắn vào lần đăng ký thi đấu *</Label>
              <Select value={registrationId} onValueChange={setRegistrationId}>
                <SelectTrigger><SelectValue placeholder="Chọn lần đăng ký" /></SelectTrigger>
                <SelectContent>
                  {registrationOptions.map((r) => (
                    <SelectItem key={r.id} value={r.id}>{r.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground mt-1">
                Tệp tin luôn gắn với 1 lần đăng ký cụ thể - cùng 1 ngựa/nài đăng ký ở nhiều cuộc đua có thể cần bộ giấy tờ khác nhau.
              </p>
            </div>

            <div>
              <Label>Loại tệp *</Label>
              <Select value={fileType} onValueChange={(v) => setFileType(v as FileType)}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {FILE_TYPE_OPTIONS.map((o) => (
                    <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="fileName">Tên hiển thị</Label>
              <Input id="fileName" value={fileName} onChange={(e) => setFileName(e.target.value)} placeholder="Tên file hiển thị" />
            </div>

            <div>
              <Label>File *</Label>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp,application/pdf"
                className="hidden"
                onChange={(e) => handleFileChange(e.target.files?.[0])}
              />
              <div
                role="button"
                tabIndex={0}
                onClick={() => fileInputRef.current?.click()}
                onKeyDown={(e) => e.key === 'Enter' && fileInputRef.current?.click()}
                className="border-2 border-dashed rounded-lg p-8 text-center cursor-pointer border-muted-foreground/25 hover:border-[#D4A017]"
              >
                <Upload className="h-8 w-8 mx-auto text-muted-foreground mb-2" />
                <p className="text-sm text-muted-foreground">
                  {selectedFile ? selectedFile.name : 'Click để chọn file (JPG, PNG, GIF, PDF, tối đa 5MB)'}
                </p>
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={uploadMutation.isPending}>
                {uploadMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Tải lên
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-files')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default FileCreatePage;
