import { useState, useEffect, useMemo } from 'react';
import { useAppSelector, useAppDispatch, fetchHorses, createHorse, updateHorseThunk, deleteHorseThunk } from '@/store';
import { HorsePayload } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Plus, Pencil, Trash2, Search, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const BREEDS = ['Thoroughbred', 'Arabian', 'Quarter Horse', 'Standardbred', 'Appaloosa'];
const STATUSES = ['active', 'rest', 'injured', 'retired'];

const emptyForm = {
  name: '',
  breed: 'Thoroughbred',
  age: 3,
  speed: 65.0,
  owner: '',
  status: 'active',
  notes: '',
};

export default function Horses() {
  const dispatch = useAppDispatch();
  const { items: horses, loading } = useAppSelector((state) => state.horses);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  useEffect(() => {
    dispatch(fetchHorses());
  }, [dispatch]);

  const filteredHorses = useMemo(() => {
    return horses.filter((horse) => {
      const matchesSearch = horse.name.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesStatus = statusFilter === 'all' || horse.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [horses, searchQuery, statusFilter]);

  const handleOpenAdd = () => {
    setEditMode(false);
    setEditId(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const handleOpenEdit = (horse: typeof horses[0]) => {
    setEditMode(true);
    setEditId(horse.id);
    setForm({
      name: horse.name,
      breed: horse.breed,
      age: horse.age,
      speed: horse.speed,
      owner: horse.owner || '',
      status: horse.status || 'active',
      notes: horse.notes || '',
    });
    setDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!form.name.trim()) {
      toast.error('Vui lòng nhập tên ngựa');
      return;
    }
    const payload: HorsePayload = {
      name: form.name,
      breed: form.breed,
      age: form.age,
      speed: form.speed,
      owner: form.owner,
      status: form.status,
      notes: form.notes,
    };
    try {
      if (editMode && editId) {
        await dispatch(updateHorseThunk({ id: editId, horse: payload })).unwrap();
        toast.success('Cập nhật ngựa thành công!');
      } else {
        await dispatch(createHorse(payload)).unwrap();
        toast.success('Thêm ngựa thành công!');
      }
      setDialogOpen(false);
    } catch {
      toast.error('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleDeleteConfirm = async () => {
    if (deleteTarget) {
      try {
        await dispatch(deleteHorseThunk(deleteTarget)).unwrap();
        toast.success('Xóa ngựa thành công!');
      } catch {
        toast.error('Không thể xóa ngựa.');
      }
    }
    setDeleteDialogOpen(false);
    setDeleteTarget(null);
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, string> = {
      active: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30',
      rest: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      injured: 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
      retired: 'bg-red-500/20 text-red-400 border-red-500/30',
    };
    return (
      <Badge variant="outline" className={variants[status] || 'bg-gray-500/20 text-gray-400'}>
        {status.charAt(0).toUpperCase() + status.slice(1)}
      </Badge>
    );
  };

  if (loading && horses.length === 0) {
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
          <h1 className="text-3xl font-bold tracking-tight">Quản lý Ngựa</h1>
          <p className="text-muted-foreground mt-1">Quản lý ngựa đua, theo dõi hiệu suất và trạng thái.</p>
        </div>
        <Button onClick={handleOpenAdd} className="bg-emerald-600 hover:bg-emerald-700 text-white">
          <Plus className="mr-2 h-4 w-4" />
          Thêm ngựa
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Tìm theo tên..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="pl-9" />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[160px]">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {STATUSES.map((s) => (
              <SelectItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Tên</TableHead>
              <TableHead>Giống</TableHead>
              <TableHead className="text-center">Tuổi</TableHead>
              <TableHead className="text-center">Tốc độ</TableHead>
              <TableHead>Chủ sở hữu</TableHead>
              <TableHead>Trạng thái</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredHorses.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">Không tìm thấy ngựa nào.</TableCell>
              </TableRow>
            ) : (
              filteredHorses.map((horse) => (
                <TableRow key={horse.id}>
                  <TableCell>{horse.id}</TableCell>
                  <TableCell className="font-medium">{horse.name}</TableCell>
                  <TableCell>{horse.breed}</TableCell>
                  <TableCell className="text-center">{horse.age}</TableCell>
                  <TableCell className="text-center">{horse.speed}</TableCell>
                  <TableCell>{horse.owner}</TableCell>
                  <TableCell>{getStatusBadge(horse.status)}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" onClick={() => handleOpenEdit(horse)} className="h-8 w-8">
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => { setDeleteTarget(horse.id); setDeleteDialogOpen(true); }} className="h-8 w-8 text-red-400 hover:text-red-300">
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
      <p className="text-sm text-muted-foreground">Hiển thị {filteredHorses.length} / {horses.length} ngựa</p>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editMode ? 'Sửa thông tin ngựa' : 'Thêm ngựa mới'}</DialogTitle>
            <DialogDescription>{editMode ? 'Cập nhật thông tin ngựa.' : 'Điền thông tin để thêm ngựa mới.'}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Tên</Label>
                <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Tên ngựa" />
              </div>
              <div className="space-y-2">
                <Label>Tuổi</Label>
                <Input type="number" value={form.age} onChange={(e) => setForm({ ...form, age: parseInt(e.target.value) || 0 })} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Giống</Label>
                <Select value={form.breed} onValueChange={(val) => setForm({ ...form, breed: val })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {BREEDS.map((breed) => (<SelectItem key={breed} value={breed}>{breed}</SelectItem>))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Tốc độ (km/h)</Label>
                <Input type="number" step="0.1" value={form.speed} onChange={(e) => setForm({ ...form, speed: parseFloat(e.target.value) || 0 })} />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Chủ sở hữu</Label>
                <Input value={form.owner} onChange={(e) => setForm({ ...form, owner: e.target.value })} placeholder="Tên chủ sở hữu" />
              </div>
              <div className="space-y-2">
                <Label>Trạng thái</Label>
                <Select value={form.status} onValueChange={(val) => setForm({ ...form, status: val })}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {STATUSES.map((s) => (<SelectItem key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</SelectItem>))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="space-y-2">
              <Label>Ghi chú</Label>
              <Textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} placeholder="Ghi chú về ngựa..." rows={3} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Hủy</Button>
            <Button onClick={handleSubmit} className="bg-emerald-600 hover:bg-emerald-700 text-white">{editMode ? 'Lưu thay đổi' : 'Thêm ngựa'}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa ngựa</AlertDialogTitle>
            <AlertDialogDescription>Bạn có chắc chắn muốn xóa ngựa này? Hành động này không thể hoàn tác.</AlertDialogDescription>
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