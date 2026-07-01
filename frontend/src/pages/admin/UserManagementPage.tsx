import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { userApi } from '@/api/userApi';
import { queryKeys } from '@/constants/queryKeys';
import { useAuthStore } from '@/store/authStore';
import { userCreateSchema, UserCreateFormData, userUpdateSchema, UserUpdateFormData } from '@/schemas/userSchema';
import { UserResponse } from '@/types/user';
import { UserStatus, Role } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { SearchBar } from '@/components/common/SearchBar';
import { StatusBadge } from '@/components/common/StatusBadge';
import { handleApiError } from '@/utils/apiHelpers';
import { formatDate } from '@/utils/formatDate';
import { useDebounce } from '@/hooks/useDebounce';
import { Lock, Unlock, Plus, Pencil, Trash2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const ROLE_LABELS: Record<Role, string> = {
  [Role.ADMIN]: 'Quản trị viên',
  [Role.ORGANIZER]: 'Ban tổ chức',
  [Role.HORSE_OWNER]: 'Chủ ngựa',
  [Role.SPECTATOR]: 'Khán giả',
};

const UserManagementPage = () => {
  const queryClient = useQueryClient();
  const currentUser = useAuthStore((s) => s.user);
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);

  const params = { page, size: 10, keyword: debouncedKeyword || undefined };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.users.list(params),
    queryFn: () => userApi.getAll(params),
  });

  const invalidateUsers = () => queryClient.invalidateQueries({ queryKey: ['users'] });

  const createForm = useForm<UserCreateFormData>({ resolver: zodResolver(userCreateSchema) });
  const editForm = useForm<UserUpdateFormData>({ resolver: zodResolver(userUpdateSchema) });

  const createMutation = useMutation({
    mutationFn: (data: UserCreateFormData) => userApi.create(data),
    onSuccess: () => {
      toast.success('Đã tạo tài khoản');
      invalidateUsers();
      setCreateOpen(false);
      createForm.reset();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UserUpdateFormData }) => userApi.update(id, data),
    onSuccess: () => {
      toast.success('Đã cập nhật tài khoản');
      invalidateUsers();
      setEditingUser(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => userApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa tài khoản');
      invalidateUsers();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string; role: Role }) => userApi.changeRole(id, role),
    onSuccess: () => {
      toast.success('Đã đổi vai trò');
      invalidateUsers();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const lockMutation = useMutation({
    mutationFn: (id: string) => userApi.lock(id),
    onSuccess: () => {
      toast.success('Đã khóa tài khoản');
      invalidateUsers();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const unlockMutation = useMutation({
    mutationFn: (id: string) => userApi.unlock(id),
    onSuccess: () => {
      toast.success('Đã mở khóa tài khoản');
      invalidateUsers();
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

  const openEditDialog = (user: UserResponse) => {
    setEditingUser(user);
    editForm.reset({ fullName: user.fullName, email: user.email, phone: user.phone ?? '' });
  };

  const columns: Column<UserResponse>[] = [
    { key: 'username', header: 'Username', render: (u) => <span className="font-medium">{u.username}</span> },
    { key: 'fullName', header: 'Họ tên' },
    { key: 'email', header: 'Email' },
    {
      key: 'role', header: 'Vai trò',
      render: (u) => (
        <Select
          value={u.role}
          disabled={u.id === currentUser?.id || roleMutation.isPending}
          onValueChange={(value) => roleMutation.mutate({ id: u.id, role: value as Role })}
        >
          <SelectTrigger className="w-[160px]">
            <SelectValue>
              <StatusBadge status={ROLE_LABELS[u.role]} colorClass={getRoleColor(u.role)} />
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            {Object.values(Role).map((r) => (
              <SelectItem key={r} value={r}>{ROLE_LABELS[r]}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      ),
    },
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
        <div className="flex items-center">
          <Button variant="ghost" size="icon" onClick={() => openEditDialog(u)} title="Sửa">
            <Pencil className="h-4 w-4" />
          </Button>

          {u.status === UserStatus.ACTIVE ? (
            <Button
              variant="ghost" size="icon" title="Khóa" disabled={u.id === currentUser?.id}
              onClick={() => lockMutation.mutate(u.id)}
            >
              <Lock className="h-4 w-4 text-red-500" />
            </Button>
          ) : (
            <Button variant="ghost" size="icon" onClick={() => unlockMutation.mutate(u.id)} title="Mở khóa">
              <Unlock className="h-4 w-4 text-green-500" />
            </Button>
          )}

          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button variant="ghost" size="icon" title="Xóa" disabled={u.id === currentUser?.id}>
                <Trash2 className="h-4 w-4 text-red-500" />
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Xóa tài khoản "{u.username}"?</AlertDialogTitle>
                <AlertDialogDescription>
                  Hành động này không thể hoàn tác. Tài khoản sẽ bị xóa khỏi hệ thống.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>Hủy</AlertDialogCancel>
                <AlertDialogAction onClick={() => deleteMutation.mutate(u.id)}>Xóa</AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
        </div>
      ),
    },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý Người dùng</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Plus className="h-4 w-4 mr-2" />Thêm người dùng
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Tạo tài khoản mới</DialogTitle></DialogHeader>
            <form onSubmit={createForm.handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
              <div>
                <Label>Tên đăng nhập *</Label>
                <Input {...createForm.register('username')} />
                {createForm.formState.errors.username && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.username.message}</p>
                )}
              </div>
              <div>
                <Label>Mật khẩu *</Label>
                <Input type="password" {...createForm.register('password')} />
                {createForm.formState.errors.password && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.password.message}</p>
                )}
              </div>
              <div>
                <Label>Họ tên *</Label>
                <Input {...createForm.register('fullName')} />
                {createForm.formState.errors.fullName && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.fullName.message}</p>
                )}
              </div>
              <div>
                <Label>Email *</Label>
                <Input type="email" {...createForm.register('email')} />
                {createForm.formState.errors.email && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.email.message}</p>
                )}
              </div>
              <div>
                <Label>Số điện thoại</Label>
                <Input {...createForm.register('phone')} />
              </div>
              <div>
                <Label>Vai trò *</Label>
                <Select onValueChange={(value) => createForm.setValue('role', value as Role)}>
                  <SelectTrigger><SelectValue placeholder="Chọn vai trò" /></SelectTrigger>
                  <SelectContent>
                    {Object.values(Role).map((r) => (
                      <SelectItem key={r} value={r}>{ROLE_LABELS[r]}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {createForm.formState.errors.role && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.role.message}</p>
                )}
              </div>
              <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Tạo tài khoản
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="mb-4">
        <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm theo tên hoặc email..." />
      </div>

      <DataTable columns={columns} data={users ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={!!editingUser} onOpenChange={(open) => !open && setEditingUser(null)}>
        <DialogContent>
          <DialogHeader><DialogTitle>Sửa tài khoản "{editingUser?.username}"</DialogTitle></DialogHeader>
          <form
            onSubmit={editForm.handleSubmit((d) => editingUser && updateMutation.mutate({ id: editingUser.id, data: d }))}
            className="space-y-4"
          >
            <div>
              <Label>Họ tên *</Label>
              <Input {...editForm.register('fullName')} />
              {editForm.formState.errors.fullName && (
                <p className="text-sm text-red-500">{editForm.formState.errors.fullName.message}</p>
              )}
            </div>
            <div>
              <Label>Email *</Label>
              <Input type="email" {...editForm.register('email')} />
              {editForm.formState.errors.email && (
                <p className="text-sm text-red-500">{editForm.formState.errors.email.message}</p>
              )}
            </div>
            <div>
              <Label>Số điện thoại</Label>
              <Input {...editForm.register('phone')} />
            </div>
            <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={updateMutation.isPending}>
              {updateMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Lưu thay đổi
            </Button>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default UserManagementPage;
