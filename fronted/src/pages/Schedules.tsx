import { useState, useEffect, useMemo } from 'react';
import { useAppSelector, useAppDispatch, fetchSchedules, createSchedule, updateScheduleThunk, deleteScheduleThunk } from '@/store';
import { SchedulePayload } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Plus, Pencil, Trash2, Search, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const STATUSES = ['upcoming', 'ongoing', 'done', 'cancelled'];

const emptyForm = {
  name: '',
  raceDate: '',
  venue: '',
  horseCount: 6,
  prize: '',
  status: 'upcoming',
};

export default function Schedules() {
  const dispatch = useAppDispatch();
  const { items: schedules, loading } = useAppSelector((state) => state.schedules);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  useEffect(() => {
    dispatch(fetchSchedules());
  }, [dispatch]);

  const filteredSchedules = useMemo(() => {
    return schedules.filter((schedule) => {
      const matchesSearch = schedule.name.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesStatus = statusFilter === 'all' || schedule.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [schedules, searchQuery, statusFilter]);

  const handleOpenAdd = () => {
    setEditMode(false);
    setEditId(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const handleOpenEdit = (schedule: typeof schedules[0]) => {
    setEditMode(true);
    setEditId(schedule.id);
    setForm({
      name: schedule.name,
      raceDate: schedule.raceDate ? schedule.raceDate.slice(0, 16) : '',
      venue: schedule.venue || '',
      horseCount: schedule.horseCount || 6,
      prize: schedule.prize || '',
      status: schedule.status || 'upcoming',
    });
    setDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!form.name.trim()) {
      toast.error('Vui lòng nhập tên cuộc đua');
      return;
    }
    const payload: SchedulePayload = {
      name: form.name,
      raceDate: form.raceDate,
      venue: form.venue,
      horseCount: form.horseCount,
      prize: form.prize,
      status: form.status,
    };
    try {
      if (editMode && editId) {
        await dispatch(updateScheduleThunk({ id: editId, schedule: payload })).unwrap();
        toast.success('Cập nhật lịch đua thành công!');
      } else {
        await dispatch(createSchedule(payload)).unwrap();
        toast.success('Thêm lịch đua thành công!');
      }
      setDialogOpen(false);
    } catch {
      toast.error('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleDeleteConfirm = async () => {
    if (deleteTarget) {
      try {
        await dispatch(deleteScheduleThunk(deleteTarget)).unwrap();
        toast.success('Xóa lịch đua thành công!');
      } catch {
        toast.error('Không thể xóa lịch đua.');
      }
    }
    setDeleteDialogOpen(false);
    setDeleteTarget(null);
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, string> = {
      upcoming: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      ongoing: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      done: 'bg-gray-500/20 text-gray-400 border-gray-500/30',
      cancelled: 'bg-red-500/20 text-red-400 border-red-500/30',
    };
    const labels: Record<string, string> = {
      upcoming: 'Sắp diễn ra',
      ongoing: 'Đang diễn ra',
      done: 'Đã hoàn thành',
      cancelled: 'Đã hủy',
    };
    return (
      <Badge variant="outline" className={variants[status] || 'bg-gray-500/20 text-gray-400'}>
        {labels[status] || status}
      </Badge>
    );
  };

  const formatDateTime = (dateStr: string) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
    });
  };

  if (loading && schedules.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin text-emerald-500" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Quản lý Lịch đua</h1>
          <p className="text-muted-foreground mt-1">Quản lý lịch trình các cuộc đua ngựa.</p>
        </div>
        <Button onClick={handleOpenAdd} className="bg-emerald-600 hover:bg-emerald-700 text-white">
          <Plus className="mr-2 h-4 w-4" />
          Thêm lịch đua
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Tìm theo tên..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="pl-9" />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {STATUSES.map((s) => (
              <SelectItem key={s} value={s}>{s === 'upcoming' ? 'Sắp diễn ra' : s === 'ongoing' ? 'Đang diễn ra' : s === 'done' ? 'Đã hoàn thành' : 'Đã hủy'}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Tên cuộc đua</TableHead>
              <TableHead>Thời gian</TableHead>
              <TableHead>Địa điểm</TableHead>
              <TableHead className="text-center">Số ngựa</TableHead>
              <TableHead>Giải thưởng</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredSchedules.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">Không tìm thấy lịch đua nào.</TableCell>
              </TableRow>
            ) : (
              filteredSchedules.map((schedule) => (
                <TableRow key={schedule.id}>
                  <TableCell>{schedule.id}</TableCell>
                  <TableCell className="font-medium">{schedule.name}</TableCell>
                  <TableCell>{formatDateTime(schedule.raceDate)}</TableCell>
                  <TableCell>{schedule.venue}</TableCell>
                  <TableCell className="text-center">{schedule.horseCount}</TableCell>
                  <TableCell>{schedule.prize}</TableCell>
                  <TableCell>{getStatusBadge(schedule.status)}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" onClick={() => handleOpenEdit(schedule)} className="h-8 w-8">
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => { setDeleteTarget(schedule.id); setDeleteDialogOpen(true); }} className="h-8 w-8 text-red-400 hover:text-red-300">
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
      <p className="text-sm text-muted-foreground">Hiển thị {filteredSchedules.length} / {schedules.length} lịch đua</p>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editMode ? 'Sửa lịch đua' : 'Thêm lịch đua mới'}</DialogTitle>
            <DialogDescription>{editMode ? 'Cập nhật thông tin lịch đua.' : 'Điền thông tin để thêm lịch đua mới.'}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label>Tên cuộc đua</Label>
              <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Tên cuộc đua" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Thời gian</Label>
                <Input type="datetime-local" value={form.raceDate} onChange={(e) => setForm({ ...form, raceDate: e.target.value })} />
              </div>
              <div className="space-y-2">
                <Label>Địa điểm</Label>
                <Input value={form.venue} onChange={(e) => setForm({ ...form, venue: e.target.value })} placeholder="Địa điểm" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Số ngựa tham gia</Label>
                <Input type="number" value={form.horseCount} onChange={(e) => setForm({ ...form, horseCount: parseInt(e.target.value) || 0 })} />
              </div>
              <div className="space-y-2">
                <Label>Giải thưởng</Label>
                <Input value={form.prize} onChange={(e) => setForm({ ...form, prize: e.target.value })} placeholder="₫ 500,000,000" />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Trạng thái</Label>
              <Select value={form.status} onValueChange={(val) => setForm({ ...form, status: val })}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {STATUSES.map((s) => (
                    <SelectItem key={s} value={s}>{s === 'upcoming' ? 'Sắp diễn ra' : s === 'ongoing' ? 'Đang diễn ra' : s === 'done' ? 'Đã hoàn thành' : 'Đã hủy'}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Hủy</Button>
            <Button onClick={handleSubmit} className="bg-emerald-600 hover:bg-emerald-700 text-white">{editMode ? 'Lưu thay đổi' : 'Thêm lịch đua'}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa lịch đua</AlertDialogTitle>
            <AlertDialogDescription>Bạn có chắc chắn muốn xóa lịch đua này? Hành động này không thể hoàn tác.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction onClick={handleDeleteConfirm} className="bg-red-600 hover:bg-red-700 text-white">Xóa</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}