import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="text-center">
        <h1 className="text-6xl font-bold text-[#D4A017] mb-4">404</h1>
        <p className="text-lg text-muted-foreground mb-6">Trang bạn tìm kiếm không tồn tại</p>
        <Link to="/">
          <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">Về trang chủ</Button>
        </Link>
      </div>
    </div>
  );
};

export default NotFoundPage;