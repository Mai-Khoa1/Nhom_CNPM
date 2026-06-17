import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { seasonApi } from '@/api/seasonApi';
import { queryKeys } from '@/constants/queryKeys';
import { seasonSchema, SeasonFormData } from '@/schemas/seasonSchema';
import { SeasonResponse } from '@/types/season';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { StatusBadge } from '@/components/common/StatusBadge';
import { getSeasonStatusColor } from '@/utils/getStatusColor';
import { formatDate } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { Plus, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const SeasonManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);

  const params = { page, size: 10 };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.seasons.list(params),
    queryFn: () => seasonApi.getAll(params),
  });

  const { register, handleSubmit, reset, formState: { errors } } = useForm<SeasonFormData>({
    resolver: zodResolver(seasonSchema),
  });

  const createMutation = useMutation({
    mutationFn: (data: SeasonFormData) => seasonApi.create(data),
    onSuccess: () => {
      toast.success('Tạo mùa giải thành công');
      queryClient.invalidateQueries({ queryKey: queryKeys.seasons.all });
      setDialogOpen(false);
      reset();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const seasons = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

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
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Plus className="h-4 w-4 mr-2" />Tạo mùa giải
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader><DialogTitle>Tạo mùa giải mới</DialogTitle></DialogHeader>
            <form onSubmit={handleSubmit((d) => createMutation.mutate(d))} className="space-y-4">
              <div>
                <Label>Tên mùa giải *</Label>
                <Input {...register('name')} />
                {errors.name && <p className="text-sm text-red-500">{errors.name.message}</p>}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label>Ngày bắt đầu *</Label>
                  <Input type="date" {...register('startDate')} />
                  {errors.startDate && <p className="text-sm text-red-500">{errors.startDate.message}</p>}
                </div>
                <div>
                  <Label>Ngày kết thúc *</Label>
                  <Input type="date" {...register('endDate')} />
                  {errors.endDate && <p className="text-sm text-red-500">{errors.endDate.message}</p>}
                </div>
              </div>
              <div>
                <Label>Mô tả</Label>
                <Input {...register('description')} />
              </div>
              <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={createMutation.isPending}>
                {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Tạo
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <DataTable columns={columns} data={seasons ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}
    </div>
  );
};

export default SeasonManagementPage;