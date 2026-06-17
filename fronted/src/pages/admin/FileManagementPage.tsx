import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FileText, Upload, Image, File } from 'lucide-react';

const FileManagementPage = () => {
  const fileCategories = [
    { icon: Image, label: 'Ảnh ngựa', type: 'HORSE_PHOTO', count: '-' },
    { icon: FileText, label: 'Hộ chiếu ngựa', type: 'PASSPORT_SCAN', count: '-' },
    { icon: File, label: 'Giấy khám sức khỏe', type: 'HEALTH_CERTIFICATE', count: '-' },
    { icon: Image, label: 'Ảnh nài', type: 'JOCKEY_AVATAR', count: '-' },
    { icon: FileText, label: 'Giấy phép nài', type: 'LICENSE_SCAN', count: '-' },
    { icon: File, label: 'Giấy khám nài', type: 'MEDICAL_CERTIFICATE', count: '-' },
    { icon: FileText, label: 'Báo cáo doping', type: 'DOPING_REPORT', count: '-' },
    { icon: File, label: 'Hồ sơ sức khỏe', type: 'HEALTH_RECORD_ATTACHMENT', count: '-' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý tệp tin</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {fileCategories.map((cat) => {
          const Icon = cat.icon;
          return (
            <Card key={cat.type} className="hover:shadow-md transition-shadow cursor-pointer">
              <CardContent className="p-4 flex items-center gap-3">
                <div className="h-10 w-10 rounded-lg bg-[#1A2B4A]/10 flex items-center justify-center">
                  <Icon className="h-5 w-5 text-[#1A2B4A]" />
                </div>
                <div>
                  <p className="font-medium text-sm">{cat.label}</p>
                  <p className="text-xs text-muted-foreground">{cat.count} tệp</p>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Tải lên tệp tin</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="border-2 border-dashed border-muted-foreground/25 rounded-lg p-12 text-center">
            <Upload className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">Kéo thả tệp vào đây hoặc click để chọn</p>
            <p className="text-xs text-muted-foreground mt-2">Hỗ trợ: JPG, JPEG, PNG, GIF, PDF (tối đa 5MB)</p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default FileManagementPage;