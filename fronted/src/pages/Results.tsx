import { useState, useEffect, useMemo } from 'react';
import { useAppSelector, useAppDispatch, fetchResults, createResult, updateResultThunk, deleteResultThunk } from '@/store';
import { ResultPayload } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@/components/ui/alert-dialog';
import { Plus, Pencil, Trash2, Search, Loader2 } from 'lucide-react';
import { toast } from 'sonner';

const emptyForm = {
  raceName: '',
  raceDate: '',
  venue: '',
  winner: '',
  jockey: '',
  time: '',
  prize: '',
};

export default function Results() {
  const dispatch = useAppDispatch();
  const { items: results, loading } = useAppSelector((state) => state.results);
  const [searchQuery, setSearchQuery] = useState('');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(emptyForm);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  useEffect(() => {
    dispatch(fetchResults());
  }, [dispatch]);

  const filteredResults = useMemo(() => {
    return results.filter((result) =>
      result.raceName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      result.winner?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [results, searchQuery]);

  const handleOpenAdd = () => {
    setEditMode(false);
    setEditId(null);
    setForm(emptyForm);
    setDialogOpen(true);
  };

  const handleOpenEdit = (result: typeof results[0]) => {
    setEditMode(true);
    setEditId(result.id);
    setForm({
      raceName: result.raceName,
      raceDate: result.raceDate ? result.raceDate.slice(0, 16) : '',
      venue: result.venue || '',
      winner: result.winner || '',
      jockey: result.jockey || '',
      time: result.time || '',
      prize: result.prize || '',
    });
    setDialogOpen(true);
  };

  const handleSubmit = async () => {
    if (!form.raceName.trim()) {
      toast.error('Vui lòng nhập tên cuộc đua');
      return;
    }
    const payload: ResultPayload = {
      raceName: form.raceName,
      raceDate: form.raceDate,
      venue: form.venue,
      winner: form.winner,
      jockey: form.jockey,
      time: form.time,
      prize: form.prize,
    };
    try {
      if (editMode && editId) {
        await dispatch(updateResultThunk({ id: editId, result: payload })).unwrap();
        toast.success('Cập nhật kết quả thành công!');
      } else {
        await dispatch(createResult(payload)).unwrap();
        toast.success('Thêm kết quả thành công!');
      }
      setDialogOpen(false);
    } catch {
      toast.error('Có lỗi xảy ra. Vui lòng thử lại.');
    }
  };

  const handleDeleteConfirm = async () => {
    if (deleteTarget) {
      try {
        await dispatch(deleteResultThunk(deleteTarget)).unwrap();
        toast.success('Xóa kết quả thành công!');
      } catch {
        toast.error('Không thể xóa kết quả.');
      }
    }
    setDeleteDialogOpen(false);
    setDeleteTarget(null);
  };

  const formatDateTime = (dateStr: string) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('vi-VN', {
      year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit',
    });
  };

  if (loading && results.length === 0) {
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
          <h1 className="text-3xl font-bold tracking-tight">Quản lý Kết quả</h1>
          <p className="text-muted-foreground mt-1">Quản lý kết quả các cuộc đua ngựa.</p>
        </div>
        <Button onClick={handleOpenAdd} className="bg-emerald-600 hover:bg-emerald-700 text-white">
          <Plus className="mr-2 h-4 w-4" />
          Thêm kết quả
        </Button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input placeholder="Tìm theo tên đua hoặc người thắng..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)} className="pl-9" />
        </div>
      </div>

      <div className="rounded-lg border bg-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Cuộc đua</TableHead>
              <TableHead>Thời gian</TableHead>
              <TableHead>Địa điểm</TableHead>
              <TableHead>Ngựa thắng</TableHead>
              <TableHead>Kỵ sĩ</TableHead>
              <TableHead>Thời gian đua</TableHead>
              <TableHead>Giải thưởng</TableHead>
              <TableHead className="text-right">Thao tác</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredResults.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} className="text-center py-8 text-muted-foreground">Không tìm thấy kết quả nào.</TableCell>
              </TableRow>
            ) : (
              filteredResults.map((result) => (
                <TableRow key={result.id}>
                  <TableCell>{result.id}</TableCell>
                  <TableCell className="font-medium">{result.raceName}</TableCell>
                  <TableCell>{formatDateTime(result.raceDate)}</TableCell>
                  <TableCell>{result.venue}</TableCell>
                  <TableCell className="font-semibold text-emerald-400">{result.winner}</TableCell>
                  <TableCell>{result.jockey}</TableCell>
                  <TableCell className="font-mono">{result.time}</TableCell>
                  <TableCell>{result.prize}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button variant="ghost" size="icon" onClick={() => handleOpenEdit(result)} className="h-8 w-8">
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button variant="ghost" size="icon" onClick={() => { setDeleteTarget(result.id); setDeleteDialogOpen(true); }} className="h-8 w-8 text-red-400 hover:text-red-300">
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
      <p className="text-sm text-muted-foreground">Hiển thị {filteredResults.length} / {results.length} kết quả</p>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>{editMode ? 'Sửa kết quả' : 'Thêm kết quả mới'}</DialogTitle>
            <DialogDescription>{editMode ? 'Cập nhật kết quả cuộc đua.' : 'Điền thông tin kết quả cuộc đua.'}</DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label>Tên cuộc đua</Label>
              <Input value={form.raceName} onChange={(e) => setForm({ ...form, raceName: e.target.value })} placeholder="Tên cuộc đua" />
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
                <Label>Ngựa thắng</Label>
                <Input value={form.winner} onChange={(e) => setForm({ ...form, winner: e.target.value })} placeholder="Tên ngựa thắng" />
              </div>
              <div className="space-y-2">
                <Label>Kỵ sĩ</Label>
                <Input value={form.jockey} onChange={(e) => setForm({ ...form, jockey: e.target.value })} placeholder="Tên kỵ sĩ" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label>Thời gian đua</Label>
                <Input value={form.time} onChange={(e) => setForm({ ...form, time: e.target.value })} placeholder="1:42.35" />
              </div>
              <div className="space-y-2">
                <Label>Giải thưởng</Label>
                <Input value={form.prize} onChange={(e) => setForm({ ...form, prize: e.target.value })} placeholder="₫ 500,000,000" />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialogOpen(false)}>Hủy</Button>
            <Button onClick={handleSubmit} className="bg-emerald-600 hover:bg-emerald-700 text-white">{editMode ? 'Lưu thay đổi' : 'Thêm kết quả'}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa kết quả</AlertDialogTitle>
            <AlertDialogDescription>Bạn có chắc chắn muốn xóa kết quả này? Hành động này không thể hoàn tác.</AlertDialogDescription>
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