import { Outlet } from 'react-router-dom';

const AuthLayout = () => {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#1A2B4A] to-[#0F1729]">
      <div className="w-full max-w-md px-4">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-[#D4A017]">🏇 Horse Racing</h1>
          <p className="text-gray-300 mt-2">Hệ thống Quản lý Giải Đua Ngựa</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl p-8">
          <Outlet />
        </div>
      </div>
    </div>
  );
};

export default AuthLayout;