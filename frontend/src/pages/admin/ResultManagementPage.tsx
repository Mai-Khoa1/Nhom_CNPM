import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { resultApi } from '@/api/resultApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceResponse } from '@/types/race';
import { RegistrationResponse } from '@/types/registration';
import { ResultDetailResponse, ResultEntryRequest } from '@/types/result';
import { RegistrationStatus, RaceStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { handleApiError } from '@/utils/apiHelpers';
import { Medal, Loader2, Send, ClipboardList } from 'lucide-react';
import { toast } from 'sonner';

interface ResultRow {
  registrationId: string;
  horseName: string;
  horseCode: string;
  jockeyName: string;
  laneNumber?: number;
  finishPosition: number | '';
  finishTime: string;
  notes: string;
}

const ResultManagementPage = () => {
  const queryClient = useQueryClient();
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null);
  const [mode, setMode] = useState<'view' | 'enter'>('view');
  const [resultRows, setResultRows] = useState<ResultRow[]>([]);

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ size: 100 }),
    queryFn: () => raceApi.getAll({ size: 100 }),
  });

  const { data: resultData, isLoading: loadingResult } = useQuery({
    queryKey: queryKeys.results.byRace(selectedRaceId!),
    queryFn: () => resultApi.getByRaceId(selectedRaceId!),
    enabled: !!selectedRaceId,
  });

  const { data: registrationsData, isLoading: loadingRegistrations } = useQuery({
    queryKey: queryKeys.races.registrations(selectedRaceId!),
    queryFn: () => raceApi.getRegistrations(selectedRaceId!, { size: 50 }),
    enabled: !!selectedRaceId,
  });

  const publishMutation = useMutation({
    mutationFn: (raceId: string) => resultApi.publish(raceId),
    onSuccess: () => {
      toast.success('Công bố kết quả thành công!');
      queryClient.invalidateQueries({ queryKey: queryKeys.results.byRace(selectedRaceId!) });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const submitResultsMutation = useMutation({
    mutationFn: (payload: ResultEntryRequest) => {
      const hasExisting = !!(result && result.details?.length > 0);
      return hasExisting
        ? resultApi.update(selectedRaceId!, payload)
        : resultApi.create(payload);
    },
    onSuccess: () => {
      toast.success('Đã lưu kết quả thành công!');
      queryClient.invalidateQueries({ queryKey: queryKeys.results.byRace(selectedRaceId!) });
      setMode('view');
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const races: RaceResponse[] = racesData?.data?.data?.content ?? [];
  const result = resultData?.data?.data;
  const registrations: RegistrationResponse[] = registrationsData?.data?.data?.content ?? [];

  const handleRaceSelect = (raceId: string) => {
    setSelectedRaceId(raceId);
    setMode('view');
    setResultRows([]);
  };

  const handleEnterMode = () => {
    const approved = registrations.filter((r) => r.status === RegistrationStatus.APPROVED);
    if (approved.length === 0) {
      toast.error('Chưa có đăng ký nào được duyệt trong cuộc đua này');
      return;
    }
    const existingByRegId: Record<string, ResultDetailResponse> = {};
    if (result?.details) {
      result.details.forEach((d) => {
        const reg = approved.find((r) => r.horseId === d.horseId);
        if (reg) existingByRegId[reg.id] = d;
      });
    }
    setResultRows(
      approved.map((reg) => {
        const existing = existingByRegId[reg.id];
        return {
          registrationId: reg.id,
          horseName: reg.horseName,
          horseCode: reg.horseCode,
          jockeyName: reg.jockeyName,
          laneNumber: reg.laneNumber,
          finishPosition: existing?.finishPosition ?? '',
          finishTime: existing?.finishTime ?? '',
          notes: existing?.notes ?? '',
        };
      })
    );
    setMode('enter');
  };

  const updateRow = (idx: number, field: keyof ResultRow, value: string | number) => {
    setResultRows((prev) =>
      prev.map((row, i) => (i === idx ? { ...row, [field]: value } : row))
    );
  };

  const handleSubmitResults = () => {
    const filledRows = resultRows.filter((r) => r.finishPosition !== '');
    if (filledRows.length === 0) {
      toast.error('Vui lòng nhập hạng cho ít nhất một ngựa');
      return;
    }
    const positions = filledRows.map((r) => Number(r.finishPosition));
    const unique = new Set(positions);
    if (unique.size !== positions.length) {
      toast.error('Các hạng không được trùng nhau');
      return;
    }
    if (result?.isPublished) {
      const confirmed = window.confirm(
        'Kết quả cuộc đua này đã được công bố. Sửa kết quả có thể ảnh hưởng tới điểm/bảng xếp hạng đã công khai. Bạn có chắc chắn muốn tiếp tục?'
      );
      if (!confirmed) return;
    }
    const payload: ResultEntryRequest = {
      raceId: selectedRaceId!,
      details: filledRows.map((r) => ({
        registrationId: r.registrationId,
        finishPosition: Number(r.finishPosition),
        finishTime: r.finishTime || undefined,
        notes: r.notes || undefined,
      })),
      confirmEditPublished: !!result?.isPublished,
    };
    submitResultsMutation.mutate(payload);
  };

  const viewColumns: Column<ResultDetailResponse>[] = [
    {
      key: 'finishPosition',
      header: 'Hạng',
      render: (r) => (
        <span className={`font-bold text-lg ${r.finishPosition <= 3 ? 'text-[#D4A017]' : ''}`}>
          #{r.finishPosition}
        </span>
      ),
    },
    { key: 'horseName', header: 'Ngựa', render: (r) => <span className="font-medium">{r.horseName} ({r.horseCode})</span> },
    { key: 'jockeyName', header: 'Nài' },
    { key: 'laneNumber', header: 'Làn', render: (r) => r.laneNumber ?? '—' },
    { key: 'finishTime', header: 'Thời gian', render: (r) => r.finishTime || '—' },
    {
      key: 'pointsEarned',
      header: 'Điểm',
      render: (r) => <span className="font-bold text-[#D4A017]">{r.pointsEarned ?? 0}</span>,
    },
  ];

  const selectedRace = races.find((r) => r.id === selectedRaceId);
  const canEnterResults = selectedRace && [RaceStatus.ONGOING, RaceStatus.COMPLETED].includes(selectedRace.status as RaceStatus);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý kết quả</h1>
        <div className="flex gap-2">
          {selectedRaceId && canEnterResults && mode === 'view' && (
            <Button
              variant="outline"
              onClick={handleEnterMode}
              disabled={loadingRegistrations}
            >
              {loadingRegistrations
                ? <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                : <ClipboardList className="h-4 w-4 mr-2" />
              }
              {result?.details?.length ? 'Sửa kết quả' : 'Nhập kết quả'}
            </Button>
          )}
          {result && !result.isPublished && selectedRaceId && mode === 'view' && (
            <Button
              className="bg-[#D4A017] hover:bg-[#C8940A] text-white"
              onClick={() => publishMutation.mutate(selectedRaceId)}
              disabled={publishMutation.isPending}
            >
              {publishMutation.isPending
                ? <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                : <Send className="h-4 w-4 mr-2" />
              }
              Công bố kết quả
            </Button>
          )}
        </div>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Chọn cuộc đua</CardTitle>
        </CardHeader>
        <CardContent>
          <Select onValueChange={handleRaceSelect} value={selectedRaceId ?? ''}>
            <SelectTrigger className="w-full md:w-96">
              <SelectValue placeholder="Chọn cuộc đua để xem kết quả" />
            </SelectTrigger>
            <SelectContent>
              {races.map((race) => (
                <SelectItem key={race.id} value={race.id}>
                  {race.name} — {race.status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {selectedRace && !canEnterResults && (
            <p className="text-sm text-muted-foreground mt-2">
              Chỉ có thể nhập kết quả khi cuộc đua đang diễn ra hoặc đã hoàn thành.
            </p>
          )}
        </CardContent>
      </Card>

      {/* VIEW MODE */}
      {selectedRaceId && mode === 'view' && (
        loadingResult ? (
          <div className="flex items-center justify-center h-32">
            <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
          </div>
        ) : result && result.details?.length > 0 ? (
          <div>
            {result.isPublished && (
              <div className="mb-4 p-3 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800">
                <p className="text-sm text-green-700 dark:text-green-400">
                  ✅ Kết quả đã được công bố
                  {result.publishedAt ? ` vào ${new Date(result.publishedAt).toLocaleString('vi-VN')}` : ''}
                </p>
              </div>
            )}
            <DataTable
              columns={viewColumns}
              data={result.details}
              isLoading={false}
              emptyMessage="Chưa có kết quả"
            />
          </div>
        ) : (
          <div className="text-center py-12">
            <Medal className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground mb-4">Chưa có kết quả cho cuộc đua này</p>
            {canEnterResults && (
              <Button onClick={handleEnterMode} className="bg-[#D4A017] hover:bg-[#C8940A] text-white">
                <ClipboardList className="h-4 w-4 mr-2" />Nhập kết quả
              </Button>
            )}
          </div>
        )
      )}

      {/* ENTER RESULTS MODE */}
      {selectedRaceId && mode === 'enter' && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">Nhập kết quả — {selectedRace?.name}</h2>
            <Button variant="outline" onClick={() => setMode('view')}>Hủy</Button>
          </div>

          {result?.isPublished && (
            <div className="mb-4 p-3 rounded-lg bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800">
              <p className="text-sm text-amber-700 dark:text-amber-400">
                ⚠️ Kết quả cuộc đua này đã được công bố. Việc sửa sẽ yêu cầu xác nhận và có thể ảnh hưởng tới điểm/bảng xếp hạng đã công khai.
              </p>
            </div>
          )}

          {loadingRegistrations ? (
            <div className="flex items-center justify-center h-32">
              <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
            </div>
          ) : resultRows.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground">Không có đăng ký nào được duyệt trong cuộc đua này</p>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="rounded-md border overflow-hidden">
                <table className="w-full text-sm">
                  <thead className="bg-muted">
                    <tr>
                      <th className="text-left p-3 font-medium">Ngựa</th>
                      <th className="text-left p-3 font-medium">Nài</th>
                      <th className="text-left p-3 font-medium w-20">Làn</th>
                      <th className="text-left p-3 font-medium w-28">Hạng *</th>
                      <th className="text-left p-3 font-medium w-36">Thời gian</th>
                      <th className="text-left p-3 font-medium w-48">Ghi chú</th>
                    </tr>
                  </thead>
                  <tbody>
                    {resultRows.map((row, idx) => (
                      <tr key={row.registrationId} className="border-t">
                        <td className="p-3">
                          <span className="font-medium">{row.horseName}</span>
                          <span className="text-muted-foreground ml-1 text-xs">({row.horseCode})</span>
                        </td>
                        <td className="p-3">{row.jockeyName}</td>
                        <td className="p-3">{row.laneNumber ?? '—'}</td>
                        <td className="p-3">
                          <Input
                            type="number"
                            min={1}
                            max={resultRows.length}
                            value={row.finishPosition}
                            onChange={(e) => updateRow(idx, 'finishPosition', e.target.value === '' ? '' : Number(e.target.value))}
                            placeholder="VD: 1"
                            className="w-20"
                          />
                        </td>
                        <td className="p-3">
                          <Input
                            value={row.finishTime}
                            onChange={(e) => updateRow(idx, 'finishTime', e.target.value)}
                            placeholder="VD: 2:15.3"
                            className="w-32"
                          />
                        </td>
                        <td className="p-3">
                          <Input
                            value={row.notes}
                            onChange={(e) => updateRow(idx, 'notes', e.target.value)}
                            placeholder="Ghi chú..."
                          />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setMode('view')}>Hủy</Button>
                <Button
                  className="bg-[#D4A017] hover:bg-[#C8940A] text-white"
                  onClick={handleSubmitResults}
                  disabled={submitResultsMutation.isPending}
                >
                  {submitResultsMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                  Lưu kết quả
                </Button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* No race selected */}
      {!selectedRaceId && (
        <div className="text-center py-12">
          <Medal className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Chọn một cuộc đua để xem hoặc nhập kết quả</p>
        </div>
      )}
    </div>
  );
};

export default ResultManagementPage;
