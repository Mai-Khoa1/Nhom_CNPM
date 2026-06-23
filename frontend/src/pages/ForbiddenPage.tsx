import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { ShieldAlert } from 'lucide-react';

const ForbiddenPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-center">
        <ShieldAlert className="h-16 w-16 text-red-500 mx-auto mb-4" />
        <h1 className="text-4xl font-bold text-foreground mb-2">403</h1>
        <p className="text-lg text-muted-foreground mb-6">Bạn không có quyền truy cập trang này</p>
        <Link to="/">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">Về trang chủ</Button>
        </Link>
      </div>
    </div>
  );
};

export default ForbiddenPage;