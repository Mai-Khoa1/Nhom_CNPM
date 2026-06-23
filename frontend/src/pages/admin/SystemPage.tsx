import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Settings, Database, Server, Globe, Shield, Clock } from 'lucide-react';

const SystemPage = () => {
  const systemInfo = [
    { icon: Server, label: 'Backend', value: 'Spring Boot 3.2.0', status: 'online' },
    { icon: Database, label: 'Database', value: 'MySQL 8.0', status: 'online' },
    { icon: Globe, label: 'API Base URL', value: import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1', status: 'config' },
    { icon: Shield, label: 'JWT Secret', value: '••••••••', status: 'secure' },
    { icon: Clock, label: 'Access Token TTL', value: '1 giờ', status: 'config' },
    { icon: Clock, label: 'Refresh Token TTL', value: '7 ngày', status: 'config' },
  ];

  const configs = [
    { label: 'Kích thước file tối đa', value: '5 MB' },
    { label: 'Định dạng cho phép', value: 'jpg, jpeg, png, gif, pdf' },
    { label: 'Storage Provider', value: 'Local' },
    { label: 'CORS Origins', value: 'localhost:5173, localhost:3000' },
    { label: 'WebSocket Endpoint', value: '/ws (STOMP + SockJS)' },
    { label: 'Swagger UI', value: '/swagger-ui.html' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Cài đặt hệ thống</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Settings className="h-5 w-5" />
              Thông tin hệ thống
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {systemInfo.map((info) => {
                const Icon = info.icon;
                return (
                  <div key={info.label} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                    <div className="flex items-center gap-3">
                      <Icon className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm">{info.label}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium">{info.value}</span>
                      {info.status === 'online' && (
                        <span className="h-2 w-2 rounded-full bg-green-500" />
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <Database className="h-5 w-5" />
              Cấu hình
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {configs.map((config) => (
                <div key={config.label} className="flex items-center justify-between p-3 rounded-lg bg-muted/50">
                  <span className="text-sm">{config.label}</span>
                  <span className="text-sm font-medium font-mono">{config.value}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default SystemPage;