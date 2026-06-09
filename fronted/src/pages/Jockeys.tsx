import { useState, useEffect, useMemo } from 'react';
import { useAppSelector, useAppDispatch, fetchJockeys, createJockey, updateJockeyThunk, deleteJockeyThunk } from '@/store';
import { JockeyPayload } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Plus, Pencil, Trash2, Search, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const emptyForm = {
  name: '',
  country: '',
  wins: 0,
  races: 0,
  bio: '',
};

function getInitials(name: string): string {
  return name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);
}

export default function Jockeys() {
  const dispatch = useAppDispatch();
  const { items: jockeys, loading } = useAppSelector((state) => state.jockeys);
  const [searchQuery, setSearchQuery] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  useEffect(() => {
    dispatch(fetchJockeys());
  }, [dispatch]);

  const filteredJockeys = useMemo(() => {
    return jockeys.filter((jockey) =>
      jockey.name.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [jockeys, searchQuery]);

  const handleOpenAdd = () => {
    setEditMode(false);
    setEditId(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const handleOpenEdit = (jockey: typeof jockeys[0]) => {
    setEditMode(true);
    setEditId(jockey.id);
    setForm({
      name: jockey.name,
      country: jockey.country || '',
      wins: jockey.wins || 0,
      races: jockey.races || 0,
      bio: jockey.bio || '',
    });
    setDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!form.name.trim()) {
      toast.error('Vui lòng nhập tên kỵ sĩ');
      return;
    }
    const payload: JockeyPayload = {
      name: form.name,
      country: form.country,
      wins: form.wins,
      races: form.races,
      bio: form.bio,
    };
    try {
      if (editMode && editId) {
        await dispatch(updateJockeyThunk({ id: editId, jockey: payload })).unwrap();
        toast.success('Cập nhật kỵ sĩ thành công!');
      } else {
        await dispatch(createJockey(payload)).unwrap();
        toast.success('Thêm kỵ sĩ thành công!');
      }
      setDialogOpen(false);
    } catch {
      toast.error('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleDeleteConfirm = async () => {
    if (deleteTarget) {
      try {
        await dispatch(deleteJockeyThunk(deleteTarget)).unwrap();
        toast.success('Xóa kỵ sĩ thành công!');
      } catch {
        toast.error('Không thể xóa kỵ sĩ.');
      }
    }
    setDeleteDialogOpen(false);
    setDeleteTarget(null);
  };

  const getWinRate = (wins: number, races: number) => {
    if (races === 0) return '0%';
    return `${((wins / races) * 100).toFixed(1)}%`;
  };

  if (loading && jockeys.length === 0) {
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
          <h1 className="text-3xl font-bold tracking-tight">Quản lý Kỵ sĩ</h1>
          <p className="text-muted-foreground mt-1">Quản lý kỵ sĩ, theo dõi thành tích.</p>
        </div>
        <Button onClick={handleOpenAdd} className="bg-emerald-600 hover:bg-emerald-700 text-white">
          <Plus className="mr-2 h-4 w-4" />
          Thêm kỵ sĩ
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Tìm theo tên..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="pl-9" />
        </div>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Kỵ sĩ</TableHead>
              <TableHead>Quốc gia</TableHead>
              <TableHead className="text-center">Thắng</TableHead>
              <TableHead className="text-center">Số đua</TableHead>
              <TableHead className="text-center">Tỷ lệ thắng</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredJockeys.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">Không tìm thấy kỵ sĩ nào.</TableCell>
              </TableRow>
            ) : (
              filteredJockeys.map((jockey) => (
                <TableRow key={jockey.id}>
                  <TableCell>{jockey.id}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-3">
                      <Avatar className="h-8 w-8">
                        <AvatarFallback className="bg-emerald-500/20 text-emerald-400 text-xs">
                          {getInitials(jockey.name)}
                        </AvatarFallback>
                      </Avatar>
                      <span className="font-medium">{jockey.name}</span>
                    </div>
                  </TableCell>
                  <TableCell>{jockey.country}</TableCell>
                  <TableCell className="text-center">{jockey.wins}</TableCell>
                  <TableCell className="text-center">{jockey.races}</TableCell>
                  <TableCell className="text-center font-semibold text-emerald-400">
                    {getWinRate(jockey.wins, jockey.races)}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" onClick={() => handleOpenEdit(jockey)} className="h-8 w-8">
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => { setDeleteTarget(jockey.id); setDeleteDialogOpen(true); }} className="h-8 w-8 text-red-400 hover:text-red-300">
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
      <p className="text-sm text-muted-foreground">Hiển thị {filteredJockeys.length} / {jockeys.length} kỵ sĩ</p>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editMode ? 'Sửa thông tin kỵ sĩ' : 'Thêm kỵ sĩ mới'}</DialogTitle>
            <DialogDescription>{editMode ? 'Cập nhật thông tin kỵ sĩ.' : 'Điền thông tin để thêm kỵ sĩ mới.'}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Tên</Label>
                <Input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Tên kỵ sĩ" />
              </div>
              <div className="space-y-2">
                <Label>Quốc gia</Label>
                <Input value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} placeholder="Việt Nam" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Số trận thắng</Label>
                <Input type="number" value={form.wins} onChange={(e) => setForm({ ...form, wins: parseInt(e.target.value) || 0 })} />
              </div>
              <div className="space-y-2">
                <Label>Tổng số đua</Label>
                <Input type="number" value={form.races} onChange={(e) => setForm({ ...form, races: parseInt(e.target.value) || 0 })} />
              </div>
            </div>
            <div className="space-y-2">
              <Label>Tiểu sử</Label>
              <Textarea value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} placeholder="Tiểu sử kỵ sĩ..." rows={3} />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Hủy</Button>
            <Button onClick={handleSubmit} className="bg-emerald-600 hover:bg-emerald-700 text-white">{editMode ? 'Lưu thay đổi' : 'Thêm kỵ sĩ'}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa kỵ sĩ</AlertDialogTitle>
            <AlertDialogDescription>Bạn có chắc chắn muốn xóa kỵ sĩ này? Hành động này không thể hoàn tác.</AlertDialogDescription>
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