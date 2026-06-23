import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/api/userApi';
import { queryKeys } from '@/constants/queryKeys';
import { UserResponse } from '@/types/user';
import { UserStatus, Role } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { SearchBar } from '@/components/common/SearchBar';
import { StatusBadge } from '@/components/common/StatusBadge';
import { handleApiError } from '@/utils/apiHelpers';
import { formatDate } from '@/utils/formatDate';
import { useDebounce } from '@/hooks/useDebounce';
import { Lock, Unlock } from 'lucide-react';
import { toast } from 'sonner';

const UserManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword);

  const params = { page, size: 10, keyword: debouncedKeyword || undefined };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.users.list(params),
    queryFn: () => userApi.getAll(params),
  });

  const lockMutation = useMutation({
    mutationFn: (id: string) => userApi.lock(id),
    onSuccess: () => {
      toast.success('Đã khóa tài khoản');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const unlockMutation = useMutation({
    mutationFn: (id: string) => userApi.unlock(id),
    onSuccess: () => {
      toast.success('Đã mở khóa tài khoản');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const users = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const getRoleColor = (role: Role) => {
    switch (role) {
      case Role.ADMIN: return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      case Role.ORGANIZER: return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
      case Role.HORSE_OWNER: return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const columns: Column<UserResponse>[] = [
    { key: 'username', header: 'Username', render: (u) => <span className="font-medium">{u.username}</span> },
    { key: 'fullName', header: 'Họ tên' },
    { key: 'email', header: 'Email' },
    { key: 'role', header: 'Vai trò', render: (u) => <StatusBadge status={u.role} colorClass={getRoleColor(u.role)} /> },
    {
      key: 'status', header: 'Trạng thái',
      render: (u) => (
        <StatusBadge
          status={u.status}
          colorClass={u.status === UserStatus.ACTIVE ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}
        />
      ),
    },
    { key: 'createdAt', header: 'Ngày tạo', render: (u) => formatDate(u.createdAt) },
    {
      key: 'actions', header: 'Thao tác',
      render: (u) => (
        <div>
          {u.status === UserStatus.ACTIVE ? (
            <Button variant="ghost" size="icon" onClick={() => lockMutation.mutate(u.id)} title="Khóa">
              <Lock className="h-4 w-4 text-red-500" />
            </Button>
          ) : (
            <Button variant="ghost" size="icon" onClick={() => unlockMutation.mutate(u.id)} title="Mở khóa">
              <Unlock className="h-4 w-4 text-green-500" />
            </Button>
          )}
        </div>
      ),
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Quản lý Người dùng</h1>

      <div className="mb-4">
        <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm theo tên hoặc email..." />
      </div>

      <DataTable columns={columns} data={users ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}
    </div>
  );
};

export default UserManagementPage;