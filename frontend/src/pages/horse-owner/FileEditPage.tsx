import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { uploadApi } from '@/api/uploadApi';
import { registrationApi } from '@/api/registrationApi';
import { queryKeys } from '@/constants/queryKeys';
import { handleApiError } from '@/utils/apiHelpers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Loader2, ArrowLeft } from 'lucide-react';
import { toast } from 'sonner';

const FileEditPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.files.detail(id ?? ''),
    queryFn: () => uploadApi.getMeta(id ?? ''),
    enabled: !!id,
  });
  const file = data?.data?.data;

  const [tenFile, setTenFile] = useState('');
  const [registrationId, setRegistrationId] = useState('');

  useEffect(() => {
    if (file) {
      setTenFile(file.fileName);
      setRegistrationId(file.targetType === 'DANG_KY' ? (file.targetId ?? '') : '');
    }
  }, [file]);

  const { data: registrationsData } = useQuery({
    queryKey: queryKeys.registrations.my({ size: 200 }),
    queryFn: () => registrationApi.getMy({ size: 200 }),
  });

  const registrationOptions = (registrationsData?.data?.data?.content ?? []).map((r) => ({
    id: r.id,
    label: `${r.horseName} - ${r.jockeyName} (${r.raceName})`,
  }));

  const mutation = useMutation({
    mutationFn: () => uploadApi.update(id ?? '', {
      tenFile,
      loaiFile: file?.fileType,
      targetType: 'DANG_KY',
      targetId: registrationId || undefined,
    }),
    onSuccess: () => {
      toast.success('Đã lưu thay đổi. Nếu đăng ký liên quan đã được duyệt, thay đổi sẽ chờ Ban tổ chức duyệt lại trước khi áp dụng.');
      queryClient.invalidateQueries({ queryKey: queryKeys.files.all });
      navigate('/my-files');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutation.mutate();
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-[#D4A017]" />
      </div>
    );
  }

  return (
    <div>
      <Button variant="ghost" onClick={() => navigate('/my-files')} className="mb-4">
        <ArrowLeft className="h-4 w-4 mr-2" />Quay lại
      </Button>

      <Card>
        <CardHeader>
          <CardTitle>Chỉnh sửa tệp tin</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={onSubmit} className="space-y-4 max-w-2xl">
            <div>
              <Label htmlFor="tenFile">Tên hiển thị *</Label>
              <Input id="tenFile" value={tenFile} onChange={(e) => setTenFile(e.target.value)} required />
            </div>

            <div>
              <Label>Gắn vào lần đăng ký thi đấu</Label>
              <Select value={registrationId} onValueChange={setRegistrationId}>
                <SelectTrigger><SelectValue placeholder="Chọn lần đăng ký" /></SelectTrigger>
                <SelectContent>
                  {registrationOptions.map((r) => (
                    <SelectItem key={r.id} value={r.id}>{r.label}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <p className="text-sm text-muted-foreground">
              Nếu lần đăng ký liên quan đã được Ban tổ chức duyệt, thay đổi này sẽ được gửi thành yêu cầu chờ duyệt lại.
            </p>

            <div className="flex gap-3 pt-4">
              <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Lưu thay đổi
              </Button>
              <Button type="button" variant="outline" onClick={() => navigate('/my-files')}>Hủy</Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
};

export default FileEditPage;
