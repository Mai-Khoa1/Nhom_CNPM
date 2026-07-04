import { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FileText, Upload, Image, File, Loader2, Download } from 'lucide-react';
import { uploadApi } from '@/api/uploadApi';
import { queryKeys } from '@/constants/queryKeys';
import { FileType } from '@/types/enums';
import { FileUploadResponse } from '@/types/upload';
import { handleApiError } from '@/utils/apiHelpers';
import { ENV } from '@/config/env';
import { toast } from 'sonner';

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5MB
const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'application/pdf'];

const TARGET_TYPE_BY_FILE_TYPE: Record<string, 'HORSE' | 'JOCKEY' | 'HEALTH_RECORD' | 'DOPING_TEST'> = {
  [FileType.HORSE_PHOTO]: 'HORSE',
  [FileType.PASSPORT_SCAN]: 'HORSE',
  [FileType.HEALTH_CERTIFICATE]: 'HORSE',
  [FileType.JOCKEY_AVATAR]: 'JOCKEY',
  [FileType.LICENSE_SCAN]: 'JOCKEY',
  [FileType.MEDICAL_CERTIFICATE]: 'JOCKEY',
  [FileType.DOPING_REPORT]: 'DOPING_TEST',
  [FileType.HEALTH_RECORD_ATTACHMENT]: 'HEALTH_RECORD',
};

const fileCategories: { icon: typeof Image; label: string; type: FileType }[] = [
  { icon: Image, label: 'Ảnh ngựa', type: FileType.HORSE_PHOTO },
  { icon: FileText, label: 'Hộ chiếu ngựa', type: FileType.PASSPORT_SCAN },
  { icon: File, label: 'Giấy khám sức khỏe', type: FileType.HEALTH_CERTIFICATE },
  { icon: Image, label: 'Ảnh nài', type: FileType.JOCKEY_AVATAR },
  { icon: FileText, label: 'Giấy phép nài', type: FileType.LICENSE_SCAN },
  { icon: File, label: 'Giấy khám nài', type: FileType.MEDICAL_CERTIFICATE },
  { icon: FileText, label: 'Báo cáo doping', type: FileType.DOPING_REPORT },
  { icon: File, label: 'Hồ sơ sức khỏe', type: FileType.HEALTH_RECORD_ATTACHMENT },
];

const FileManagementPage = () => {
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedType, setSelectedType] = useState<FileType>(FileType.HORSE_PHOTO);
  const [isDragging, setIsDragging] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [previewName, setPreviewName] = useState<string | null>(null);

  const { data: filesData, isLoading } = useQuery({
    queryKey: queryKeys.files.list(selectedType),
    queryFn: () => uploadApi.list({ fileType: selectedType }),
  });
  const files: FileUploadResponse[] = filesData?.data?.data ?? [];

  const uploadMutation = useMutation({
    mutationFn: (file: File) =>
      uploadApi.upload({ file, fileType: selectedType, targetType: TARGET_TYPE_BY_FILE_TYPE[selectedType] }),
    onSuccess: () => {
      toast.success('Tải file lên thành công');
      queryClient.invalidateQueries({ queryKey: queryKeys.files.list(selectedType) });
      setPreviewUrl(null);
      setPreviewName(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const validateAndUpload = (file: File | undefined | null) => {
    if (!file) return;
    if (file.size > MAX_FILE_SIZE_BYTES) {
      toast.error('File vượt quá dung lượng tối đa cho phép (5MB)');
      return;
    }
    if (!ALLOWED_MIME_TYPES.includes(file.type)) {
      toast.error('Định dạng file không được hỗ trợ. Chỉ chấp nhận JPG, PNG, GIF, WEBP hoặc PDF.');
      return;
    }

    if (file.type.startsWith('image/')) {
      setPreviewUrl(URL.createObjectURL(file));
    } else {
      setPreviewUrl(null);
    }
    setPreviewName(file.name);
    uploadMutation.mutate(file);
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    validateAndUpload(e.target.files?.[0]);
    e.target.value = '';
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    validateAndUpload(e.dataTransfer.files?.[0]);
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý tệp tin</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {fileCategories.map((cat) => {
          const Icon = cat.icon;
          const isSelected = cat.type === selectedType;
          return (
            <Card
              key={cat.type}
              onClick={() => setSelectedType(cat.type)}
              className={`hover:shadow-md transition-shadow cursor-pointer ${
                isSelected ? 'ring-2 ring-[#1A2B4A]' : ''
              }`}
            >
              <CardContent className="p-4 flex items-center gap-3">
                <div className="h-10 w-10 rounded-lg bg-[#1A2B4A]/10 flex items-center justify-center">
                  <Icon className="h-5 w-5 text-[#1A2B4A]" />
                </div>
                <div>
                  <p className="font-medium text-sm">{cat.label}</p>
                  <p className="text-xs text-muted-foreground">
                    {isLoading && isSelected ? '...' : cat.type === selectedType ? files.length : '-'} tệp
                  </p>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">
            Tải lên tệp tin — {fileCategories.find((c) => c.type === selectedType)?.label}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/gif,image/webp,application/pdf"
            className="hidden"
            onChange={handleFileInputChange}
          />
          <div
            role="button"
            tabIndex={0}
            onClick={() => fileInputRef.current?.click()}
            onKeyDown={(e) => e.key === 'Enter' && fileInputRef.current?.click()}
            onDragOver={(e) => {
              e.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
            className={`border-2 border-dashed rounded-lg p-12 text-center cursor-pointer transition-colors ${
              isDragging ? 'border-[#D4A017] bg-[#D4A017]/5' : 'border-muted-foreground/25'
            }`}
          >
            {uploadMutation.isPending ? (
              <Loader2 className="h-12 w-12 mx-auto text-[#D4A017] mb-4 animate-spin" />
            ) : previewUrl ? (
              <img src={previewUrl} alt="preview" className="h-24 mx-auto mb-4 rounded object-cover" />
            ) : (
              <Upload className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            )}
            <p className="text-muted-foreground">
              {uploadMutation.isPending
                ? 'Đang tải lên...'
                : previewName
                ? previewName
                : 'Kéo thả tệp vào đây hoặc click để chọn'}
            </p>
            <p className="text-xs text-muted-foreground mt-2">Hỗ trợ: JPG, JPEG, PNG, GIF, PDF (tối đa 5MB)</p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Danh sách tệp đã tải lên</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center h-24">
              <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
            </div>
          ) : files.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-6">Chưa có tệp nào được tải lên</p>
          ) : (
            <ul className="divide-y">
              {files.map((f) => (
                <li key={f.fileId} className="flex items-center justify-between py-3">
                  <div className="flex items-center gap-3 min-w-0">
                    <File className="h-5 w-5 text-muted-foreground shrink-0" />
                    <div className="min-w-0">
                      <p className="text-sm font-medium truncate">{f.fileName}</p>
                      <p className="text-xs text-muted-foreground">{(f.fileSize / 1024).toFixed(1)} KB</p>
                    </div>
                  </div>
                  <a
                    href={`${ENV.API_URL}${f.url}`}
                    target="_blank"
                    rel="noreferrer"
                    className="text-[#1A2B4A] hover:text-[#D4A017] shrink-0"
                  >
                    <Download className="h-4 w-4" />
                  </a>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default FileManagementPage;
