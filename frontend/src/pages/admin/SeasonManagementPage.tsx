import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { seasonApi } from '@/api/seasonApi';
import { queryKeys } from '@/constants/queryKeys';
import { seasonSchema, SeasonFormData } from '@/schemas/seasonSchema';
import { SeasonResponse } from '@/types/season';
import { SeasonStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import {
  AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent,
  AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getSeasonStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, Loader2, Pencil, Trash2, Lock } from 'lucide-react';
import { toast } from 'sonner';

const SeasonManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [viewingSeason, setViewingSeason] = useState<SeasonResponse | null>(null);
  const [isEditing, setIsEditing] = useState(false);

  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.seasons.list(params),
    queryFn: () => seasonApi.getAll(params),
  });

  const createForm = useForm<SeasonFormData>({ resolver: zodResolver(seasonSchema) });
  const editForm = useForm<SeasonFormData>({ resolver: zodResolver(seasonSchema) });

  const invalidateSeasons = () => queryClient.invalidateQueries({ queryKey: queryKeys.seasons.all });

  const createMutation = useMutation({
    mutationFn: (data: SeasonFormData) => seasonApi.create(data),
    onSuccess: () => {
      toast.success('Tạo mùa giải thành công');
      invalidateSeasons();
      setCreateOpen(false);
      createForm.reset();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: SeasonFormData }) => seasonApi.update(id, data),
    onSuccess: (response) => {
      toast.success('Đã cập nhật mùa giải');
      invalidateSeasons();
      setViewingSeason(response.data.data);
      setIsEditing(false);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => seasonApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa mùa giải');
      invalidateSeasons();
      setViewingSeason(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const closeMutation = useMutation({
    mutationFn: (id: string) => seasonApi.close(id),
    onSuccess: () => {
      toast.success('Đã đóng mùa giải');
      invalidateSeasons();
      setViewingSeason(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const seasons = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const openDetail = (season: SeasonResponse) => {
    setViewingSeason(season);
    setIsEditing(false);
  };

  const startEditing = () => {
    if (!viewingSeason) return;
    editForm.reset({
      name: viewingSeason.name,
      startDate: viewingSeason.startDate,
      endDate: viewingSeason.endDate,
      description: viewingSeason.description ?? '',
    });
    setIsEditing(true);
  };

  const columns: Column<SeasonResponse>[] = [
    { key: 'name', header: 'Tên mùa giải', render: (s) => <span className="font-medium">{s.name}</span> },
    { key: 'startDate', header: 'Bắt đầu', render: (s) => formatDate(s.startDate) },
    { key: 'endDate', header: 'Kết thúc', render: (s) => formatDate(s.endDate) },
    {
      key: 'status', header: 'Trạng thái',
      render: (s) => <StatusBadge status={s.status} colorClass={getSeasonStatusColor(s.status)} />,
    },
    { key: 'description', header: 'Mô tả', render: (s) => s.description || '-' },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý Mùa giải</h1>
        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Plus className="h-4 w-4 mr-2" />Tạo mùa giải
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Tạo mùa giải mới</DialogTitle></DialogHeader>
            <form onSubmit={createForm.handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
              <div>
                <Label>Tên mùa giải *</Label>
                <Input {...createForm.register('name')} />
                {createForm.formState.errors.name && (
                  <p className="text-sm text-red-500">{createForm.formState.errors.name.message}</p>
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Ngày bắt đầu *</Label>
                  <Input type="date" {...createForm.register('startDate')} />
                  {createForm.formState.errors.startDate && (
                    <p className="text-sm text-red-500">{createForm.formState.errors.startDate.message}</p>
                  )}
                </div>
                <div>
                  <Label>Ngày kết thúc *</Label>
                  <Input type="date" {...createForm.register('endDate')} />
                  {createForm.formState.errors.endDate && (
                    <p className="text-sm text-red-500">{createForm.formState.errors.endDate.message}</p>
                  )}
                </div>
              </div>
              <div>
                <Label>Mô tả</Label>
                <Input {...createForm.register('description')} />
              </div>
              <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Tạo
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <DataTable columns={columns} data={seasons ?? []} isLoading={isLoading} onRowClick={openDetail} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}

      <Dialog open={!!viewingSeason} onOpenChange={(open) => { if (!open) { setViewingSeason(null); setIsEditing(false); } }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Sửa mùa giải' : 'Thông tin mùa giải'}</DialogTitle>
          </DialogHeader>

          {viewingSeason && !isEditing && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-muted-foreground">Tên mùa giải</p>
                  <p className="font-medium">{viewingSeason.name}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Trạng thái</p>
                  <StatusBadge status={viewingSeason.status} colorClass={getSeasonStatusColor(viewingSeason.status)} />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngày bắt đầu</p>
                  <p className="font-medium">{formatDate(viewingSeason.startDate)}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngày kết thúc</p>
                  <p className="font-medium">{formatDate(viewingSeason.endDate)}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Ngày tạo</p>
                  <p className="font-medium">{formatDate(viewingSeason.createdAt)}</p>
                </div>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Mô tả</p>
                <p className="font-medium">{viewingSeason.description || '-'}</p>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <AlertDialog>
                  <AlertDialogTrigger asChild>
                    <Button variant="outline">
                      <Trash2 className="h-4 w-4 mr-2 text-red-500" />Xóa
                    </Button>
                  </AlertDialogTrigger>
                  <AlertDialogContent>
                    <AlertDialogHeader>
                      <AlertDialogTitle>Xóa mùa giải "{viewingSeason.name}"?</AlertDialogTitle>
                      <AlertDialogDescription>
                        Hành động này không thể hoàn tác. Mùa giải chỉ có thể xóa khi chưa có chặng đua nào thuộc về nó.
                      </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                      <AlertDialogCancel>Hủy</AlertDialogCancel>
                      <AlertDialogAction onClick={() => deleteMutation.mutate(viewingSeason.id)}>Xóa</AlertDialogAction>
                    </AlertDialogFooter>
                  </AlertDialogContent>
                </AlertDialog>
                {viewingSeason.status !== SeasonStatus.CLOSED && (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="outline" disabled={closeMutation.isPending}>
                        {closeMutation.isPending ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Lock className="h-4 w-4 mr-2" />}
                        Đóng mùa giải
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Đóng mùa giải "{viewingSeason.name}"?</AlertDialogTitle>
                        <AlertDialogDescription>
                          Mùa giải sẽ chuyển sang trạng thái kết thúc. Các cuộc đua trong mùa giải sẽ không thể đăng ký thêm.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Hủy</AlertDialogCancel>
                        <AlertDialogAction onClick={() => closeMutation.mutate(viewingSeason.id)}>Xác nhận đóng</AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}
                <Button onClick={startEditing} className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
                  <Pencil className="h-4 w-4 mr-2" />Sửa
                </Button>
              </div>
            </div>
          )}

          {viewingSeason && isEditing && (
            <form
              onSubmit={editForm.handleSubmit((d) => updateMutation.mutate({ id: viewingSeason.id, data: d }))}
              className="space-y-4"
            >
              <div>
                <Label>Tên mùa giải *</Label>
                <Input {...editForm.register('name')} />
                {editForm.formState.errors.name && (
                  <p className="text-sm text-red-500">{editForm.formState.errors.name.message}</p>
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Ngày bắt đầu *</Label>
                  <Input type="date" {...editForm.register('startDate')} />
                  {editForm.formState.errors.startDate && (
                    <p className="text-sm text-red-500">{editForm.formState.errors.startDate.message}</p>
                  )}
                </div>
                <div>
                  <Label>Ngày kết thúc *</Label>
                  <Input type="date" {...editForm.register('endDate')} />
                  {editForm.formState.errors.endDate && (
                    <p className="text-sm text-red-500">{editForm.formState.errors.endDate.message}</p>
                  )}
                </div>
              </div>
              <div>
                <Label>Mô tả</Label>
                <Input {...editForm.register('description')} />
              </div>
              <div className="flex justify-end gap-2">
                <Button type="button" variant="outline" onClick={() => setIsEditing(false)}>Hủy</Button>
                <Button type="submit" className="bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={updateMutation.isPending}>
                  {updateMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Lưu thay đổi
                </Button>
              </div>
            </form>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default SeasonManagementPage;
