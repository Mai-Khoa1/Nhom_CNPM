import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { raceApi } from '@/api/raceApi';
import { seasonApi } from '@/api/seasonApi';
import { queryKeys } from '@/constants/queryKeys';
import { raceSchema, RaceFormData } from '@/schemas/raceSchema';
import { RaceResponse } from '@/types/race';
import { SeasonResponse } from '@/types/season';
import { RaceStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
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
import { getRaceStatusColor } from '@/utils/getStatusColor';
import { formatDateTime } from '@/utils/formatDate';
import { handleApiError } from '@/utils/apiHelpers';
import { useDebounce } from '@/hooks/useDebounce';
import { Plus, Loader2, Pencil, Trash2, PlayCircle, Lock, Ban } from 'lucide-react';
import { toast } from 'sonner';

const STATUS_LABELS: Partial<Record<RaceStatus, string>> = {
  [RaceStatus.OPEN]: 'Mở đăng ký',
  [RaceStatus.CLOSED]: 'Đã đóng đăng ký',
  [RaceStatus.ONGOING]: 'Đang đua',
  [RaceStatus.COMPLETED]: 'Hoàn thành',
  [RaceStatus.CANCELLED]: 'Đã hủy',
};

interface RaceFormProps {
  form: ReturnType<typeof useForm<RaceFormData>>;
  seasons: SeasonResponse[];
  seasonId: string;
  onSeasonChange: (id: string) => void;
  onSubmit: (data: RaceFormData) => void;
  isPending: boolean;
  submitLabel?: string;
}

const RaceForm = ({ form, seasons, seasonId, onSeasonChange, onSubmit, isPending, submitLabel = 'Lưu' }: RaceFormProps) => (
  <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
    <div>
      <Label>Mùa giải *</Label>
      <Select value={seasonId} onValueChange={(v) => { onSeasonChange(v); form.setValue('seasonId', v, { shouldValidate: true }); }}>
        <SelectTrigger><SelectValue placeholder="Chọn mùa giải" /></SelectTrigger>
        <SelectContent>
          {seasons.map((s) => (
            <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>
          ))}
        </SelectContent>
      </Select>
      {form.formState.errors.seasonId && (
        <p className="text-sm text-red-500">{form.formState.errors.seasonId.message}</p>
      )}
    </div>
    <div>
      <Label>Tên cuộc đua *</Label>
      <Input {...form.register('name')} placeholder="VD: Chặng đua vô địch mùa xuân" />
      {form.formState.errors.name && (
        <p className="text-sm text-red-500">{form.formState.errors.name.message}</p>
      )}
    </div>
    <div className="grid grid-cols-2 gap-4">
      <div>
        <Label>Ngày & giờ đua *</Label>
        <Input type="datetime-local" {...form.register('raceDate')} />
        {form.formState.errors.raceDate && (
          <p className="text-sm text-red-500">{form.formState.errors.raceDate.message}</p>
        )}
      </div>
      <div>
        <Label>Địa điểm *</Label>
        <Input {...form.register('location')} placeholder="VD: Trường đua Phú Thọ" />
        {form.formState.errors.location && (
          <p className="text-sm text-red-500">{form.formState.errors.location.message}</p>
        )}
      </div>
      <div>
        <Label>Cự ly (m) *</Label>
        <Input type="number" min={100} max={10000} {...form.register('distance', { valueAsNumber: true })} />
        {form.formState.errors.distance && (
          <p className="text-sm text-red-500">{form.formState.errors.distance.message}</p>
        )}
      </div>
      <div>
        <Label>Số ngựa tối đa *</Label>
        <Input type="number" min={2} max={30} {...form.register('maxHorses', { valueAsNumber: true })} />
        {form.formState.errors.maxHorses && (
          <p className="text-sm text-red-500">{form.formState.errors.maxHorses.message}</p>
        )}
      </div>
      <div className="col-span-2">
        <Label>Số đăng ký đã duyệt tối thiểu để bắt đầu đua</Label>
        <Input type="number" min={2} max={30} placeholder="Mặc định: 2" {...form.register('minHorses', { valueAsNumber: true })} />
        {form.formState.errors.minHorses && (
          <p className="text-sm text-red-500">{form.formState.errors.minHorses.message}</p>
        )}
        <p className="text-xs text-muted-foreground mt-1">Bỏ trống để dùng mặc định (2). Cuộc đua chỉ chuyển sang "Đang đua" khi đủ số đăng ký đã duyệt và đã phân làn đầy đủ.</p>
      </div>
    </div>
    <div>
      <Label>Mô tả</Label>
      <Textarea {...form.register('description')} rows={2} placeholder="Thông tin thêm về cuộc đua..." />
    </div>
    <Button type="submit" className="w-full bg-[#D4A017] hover:bg-[#C8940A] text-white" disabled={isPending}>
      {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
      {submitLabel}
    </Button>
  </form>
);

const RaceManagementPage = () => {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [createOpen, setCreateOpen] = useState(false);
  const [createSeasonId, setCreateSeasonId] = useState('');
  const [viewingRace, setViewingRace] = useState<RaceResponse | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editSeasonId, setEditSeasonId] = useState('');
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState('');
  const debouncedKeyword = useDebounce(keyword);

  const params = {
    page,
    size: 10,
    keyword: debouncedKeyword || undefined,
    status: statusFilter !== 'ALL' ? statusFilter : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.races.list(params),
    queryFn: () => raceApi.getAll(params),
  });

  const { data: seasonsData } = useQuery({
    queryKey: queryKeys.seasons.list({ size: 100 }),
    queryFn: () => seasonApi.getAll({ size: 100 }),
  });

  const createForm = useForm<RaceFormData>({ resolver: zodResolver(raceSchema) });
  const editForm = useForm<RaceFormData>({ resolver: zodResolver(raceSchema) });

  const invalidateRaces = () => queryClient.invalidateQueries({ queryKey: queryKeys.races.all });

  const createMutation = useMutation({
    mutationFn: (data: RaceFormData) => raceApi.create(data),
    onSuccess: () => {
      toast.success('Tạo cuộc đua thành công');
      invalidateRaces();
      setCreateOpen(false);
      setCreateSeasonId('');
      createForm.reset();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: RaceFormData }) => raceApi.update(id, data),
    onSuccess: (response) => {
      toast.success('Đã cập nhật cuộc đua');
      invalidateRaces();
      setViewingRace(response.data.data);
      setIsEditing(false);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => raceApi.delete(id),
    onSuccess: () => {
      toast.success('Đã xóa cuộc đua');
      invalidateRaces();
      setViewingRace(null);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const publishMutation = useMutation({
    mutationFn: (id: string) => raceApi.publish(id),
    onSuccess: (response) => {
      toast.success('Đã cập nhật trạng thái cuộc đua');
      invalidateRaces();
      setViewingRace(response.data.data);
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => raceApi.cancel(id, reason),
    onSuccess: (response) => {
      toast.success('Đã hủy cuộc đua');
      invalidateRaces();
      setViewingRace(response.data.data);
      setCancelDialogOpen(false);
      setCancelReason('');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const races = data?.data?.data?.content;
  const pageInfo = data?.data?.data;
  const seasons: SeasonResponse[] = seasonsData?.data?.data?.content ?? [];

  const openDetail = (race: RaceResponse) => {
    setViewingRace(race);
    setIsEditing(false);
  };

  const startEditing = () => {
    if (!viewingRace) return;
    const raceDateStr = viewingRace.raceDate
      ? new Date(viewingRace.raceDate).toISOString().slice(0, 16)
      : '';
    setEditSeasonId(viewingRace.seasonId ?? '');
    editForm.reset({
      seasonId: viewingRace.seasonId ?? '',
      name: viewingRace.name,
      raceDate: raceDateStr,
      location: viewingRace.location,
      distance: viewingRace.distance,
      maxHorses: viewingRace.maxHorses,
      minHorses: viewingRace.minHorses,
      description: viewingRace.description ?? '',
    });
    setIsEditing(true);
  };

  const columns: Column<RaceResponse>[] = [
    { key: 'name', header: 'Tên cuộc đua', render: (r) => <span className="font-medium">{r.name}</span> },
    { key: 'seasonName', header: 'Mùa giải' },
    { key: 'raceDate', header: 'Ngày đua', render: (r) => r.raceDate ? formatDateTime(r.raceDate) : '—' },
    { key: 'location', header: 'Địa điểm' },
    { key: 'distance', header: 'Cự ly', render: (r) => `${r.distance}m` },
    { key: 'registered', header: 'Đăng ký', render: (r) => `${r.registeredCount}/${r.maxHorses ?? '∞'}` },
    {
      key: 'status', header: 'Trạng thái',
      render: (r) => <StatusBadge status={r.status} colorClass={getRaceStatusColor(r.status)} />,
    },
  ];

  const canPublish = viewingRace?.status === RaceStatus.OPEN;
  const canClose = viewingRace?.status === RaceStatus.ONGOING;
  const canEdit = viewingRace?.status === RaceStatus.OPEN;
  const canCancelRace = viewingRace?.status === RaceStatus.OPEN || viewingRace?.status === RaceStatus.ONGOING;
  const hasRegistrations = (viewingRace?.registeredCount ?? 0) > 0;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý Cuộc đua</h1>
        <Dialog open={createOpen} onOpenChange={(open) => { setCreateOpen(open); if (!open) { setCreateSeasonId(''); createForm.reset(); } }}>
          <DialogTrigger asChild>
            <Button className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
              <Plus className="h-4 w-4 mr-2" />Tạo cuộc đua
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-lg">
            <DialogHeader><DialogTitle>Tạo cuộc đua mới</DialogTitle></DialogHeader>
            <RaceForm
              form={createForm}
              seasons={seasons}
              seasonId={createSeasonId}
              onSeasonChange={setCreateSeasonId}
              onSubmit={(d) => createMutation.mutate(d)}
              isPending={createMutation.isPending}
              submitLabel="Tạo cuộc đua"
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex flex-col md:flex-row gap-4 mb-4">
        <div className="flex-1">
          <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm cuộc đua..." />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            {Object.entries(STATUS_LABELS).map(([key, label]) => (
              <SelectItem key={key} value={key}>{label}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={races ?? []} isLoading={isLoading} onRowClick={openDetail} />

      {pageInfo && (
        <PaginationControl
          page={pageInfo.page}
          totalPages={pageInfo.totalPages}
          totalElements={pageInfo.totalElements}
          onPageChange={setPage}
        />
      )}

      {/* Detail / Edit Dialog */}
      <Dialog
        open={!!viewingRace}
        onOpenChange={(open) => { if (!open) { setViewingRace(null); setIsEditing(false); } }}
      >
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>{isEditing ? 'Sửa cuộc đua' : 'Thông tin cuộc đua'}</DialogTitle>
          </DialogHeader>

          {viewingRace && !isEditing && (
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <p className="text-muted-foreground">Tên cuộc đua</p>
                  <p className="font-medium">{viewingRace.name}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Mùa giải</p>
                  <p className="font-medium">{viewingRace.seasonName ?? '—'}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Ngày & giờ đua</p>
                  <p className="font-medium">{viewingRace.raceDate ? formatDateTime(viewingRace.raceDate) : '—'}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Địa điểm</p>
                  <p className="font-medium">{viewingRace.location}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Cự ly</p>
                  <p className="font-medium">{viewingRace.distance}m</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Đăng ký</p>
                  <p className="font-medium">{viewingRace.registeredCount}/{viewingRace.maxHorses ?? '∞'}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">Đã duyệt / tối thiểu để đua</p>
                  <p className="font-medium">{viewingRace.approvedCount}/{viewingRace.minHorses}</p>
                </div>
                <div className="col-span-2">
                  <p className="text-muted-foreground">Trạng thái</p>
                  <StatusBadge status={viewingRace.status} colorClass={getRaceStatusColor(viewingRace.status)} />
                </div>
                {viewingRace.description && (
                  <div className="col-span-2">
                    <p className="text-muted-foreground">Mô tả</p>
                    <p className="font-medium">{viewingRace.description}</p>
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-2 pt-2 flex-wrap">
                {(canPublish || canClose) && (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="outline" className="text-blue-600" disabled={publishMutation.isPending}>
                        {canPublish
                          ? <><PlayCircle className="h-4 w-4 mr-2" />Bắt đầu đua</>
                          : <><Lock className="h-4 w-4 mr-2" />Kết thúc đua</>
                        }
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>
                          {canPublish ? 'Bắt đầu cuộc đua?' : 'Kết thúc cuộc đua?'}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                          {canPublish
                            ? `Chuyển trạng thái sang "Đang đua". Cần tối thiểu ${viewingRace.minHorses} đăng ký đã duyệt và tất cả phải đã được phân làn - hệ thống hiện có ${viewingRace.approvedCount} đăng ký đã duyệt. Sau khi bắt đầu, không thể duyệt đăng ký hoặc sửa làn/thông tin cuộc đua nữa.`
                            : 'Chuyển trạng thái sang "Hoàn thành". Có thể nhập kết quả sau bước này.'
                          }
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Hủy</AlertDialogCancel>
                        <AlertDialogAction onClick={() => publishMutation.mutate(viewingRace.id)}>
                          Xác nhận
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                )}

                {!hasRegistrations ? (
                  <AlertDialog>
                    <AlertDialogTrigger asChild>
                      <Button variant="outline" disabled={deleteMutation.isPending}>
                        <Trash2 className="h-4 w-4 mr-2 text-red-500" />Xóa
                      </Button>
                    </AlertDialogTrigger>
                    <AlertDialogContent>
                      <AlertDialogHeader>
                        <AlertDialogTitle>Xóa cuộc đua "{viewingRace.name}"?</AlertDialogTitle>
                        <AlertDialogDescription>
                          Hành động này không thể hoàn tác. Chỉ áp dụng vì cuộc đua này chưa có đăng ký nào.
                        </AlertDialogDescription>
                      </AlertDialogHeader>
                      <AlertDialogFooter>
                        <AlertDialogCancel>Hủy</AlertDialogCancel>
                        <AlertDialogAction
                          className="bg-red-600 hover:bg-red-700"
                          onClick={() => deleteMutation.mutate(viewingRace.id)}
                        >
                          Xóa
                        </AlertDialogAction>
                      </AlertDialogFooter>
                    </AlertDialogContent>
                  </AlertDialog>
                ) : canCancelRace ? (
                  <Dialog open={cancelDialogOpen} onOpenChange={(open) => { setCancelDialogOpen(open); if (!open) setCancelReason(''); }}>
                    <DialogTrigger asChild>
                      <Button variant="outline" className="text-red-600" disabled={cancelMutation.isPending}>
                        <Ban className="h-4 w-4 mr-2" />Hủy cuộc đua
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Hủy cuộc đua "{viewingRace.name}"?</DialogTitle>
                      </DialogHeader>
                      <p className="text-sm text-muted-foreground">
                        Cuộc đua này đã có đăng ký nên không thể xóa cứng. Mọi đăng ký đang Chờ duyệt/Đã duyệt
                        sẽ tự động chuyển sang Đã hủy và Chủ ngựa liên quan sẽ nhận thông báo.
                      </p>
                      <div>
                        <Label>Lý do hủy *</Label>
                        <Textarea value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} rows={3} placeholder="VD: Thời tiết xấu, sự cố trường đua..." />
                      </div>
                      <div className="flex justify-end gap-2">
                        <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>Đóng</Button>
                        <Button
                          className="bg-red-600 hover:bg-red-700 text-white"
                          disabled={!cancelReason.trim() || cancelMutation.isPending}
                          onClick={() => cancelMutation.mutate({ id: viewingRace.id, reason: cancelReason })}
                        >
                          {cancelMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                          Xác nhận hủy
                        </Button>
                      </div>
                    </DialogContent>
                  </Dialog>
                ) : null}

                {canEdit && (
                  <Button onClick={startEditing} className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
                    <Pencil className="h-4 w-4 mr-2" />Sửa
                  </Button>
                )}
              </div>
              {!canEdit && (
                <p className="text-xs text-muted-foreground text-right">
                  Cuộc đua đã ở trạng thái {viewingRace.status} - không thể sửa thông tin nữa.
                </p>
              )}
            </div>
          )}

          {viewingRace && isEditing && (
            <div className="space-y-2">
              <RaceForm
                form={editForm}
                seasons={seasons}
                seasonId={editSeasonId}
                onSeasonChange={setEditSeasonId}
                onSubmit={(d) => updateMutation.mutate({ id: viewingRace.id, data: d })}
                isPending={updateMutation.isPending}
                submitLabel="Lưu thay đổi"
              />
              <Button variant="outline" onClick={() => setIsEditing(false)} className="w-full">
                Hủy
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default RaceManagementPage;
