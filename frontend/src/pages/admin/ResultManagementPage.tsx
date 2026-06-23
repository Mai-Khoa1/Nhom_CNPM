import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { resultApi } from '@/api/resultApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceResponse } from '@/types/race';
import { ResultDetailResponse } from '@/types/result';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { handleApiError } from '@/utils/apiHelpers';
import { Medal, Loader2, Send } from 'lucide-react';
import { toast } from 'sonner';

const ResultManagementPage = () => {
  const queryClient = useQueryClient();
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null);

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ size: 100 }),
    queryFn: () => raceApi.getAll({ size: 100 }),
  });

  const { data: resultData, isLoading: loadingResult } = useQuery({
    queryKey: ['results', selectedRaceId],
    queryFn: () => resultApi.getByRaceId(selectedRaceId!),
    enabled: !!selectedRaceId,
  });

  const publishMutation = useMutation({
    mutationFn: (raceId: string) => resultApi.publish(raceId),
    onSuccess: () => {
      toast.success('Công bố kết quả thành công!');
      queryClient.invalidateQueries({ queryKey: ['results', selectedRaceId] });
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const races = racesData?.data?.data?.content ?? [];
  const result = resultData?.data?.data;

  const columns: Column<ResultDetailResponse>[] = [
    { key: 'finishPosition', header: 'Hạng', render: (r) => (
      <span className={`font-bold text-lg ${r.finishPosition <= 3 ? 'text-[#D4A017]' : ''}`}>
        #{r.finishPosition}
      </span>
    )},
    { key: 'horseName', header: 'Ngựa', render: (r) => <span className="font-medium">{r.horseName} ({r.horseCode})</span> },
    { key: 'jockeyName', header: 'Nài' },
    { key: 'laneNumber', header: 'Làn', render: (r) => r.laneNumber ?? '-' },
    { key: 'finishTime', header: 'Thời gian', render: (r) => r.finishTime || '-' },
    { key: 'pointsEarned', header: 'Điểm', render: (r) => <span className="font-bold text-[#D4A017]">{r.pointsEarned}</span> },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý kết quả</h1>
        {result && !result.isPublished && selectedRaceId && (
          <Button
            className="bg-[#D4A017] hover:bg-[#C8940A] text-white"
            onClick={() => publishMutation.mutate(selectedRaceId)}
            disabled={publishMutation.isPending}
          >
            {publishMutation.isPending ? <Loader2 className="h-4 w-4 mr-2 animate-spin" /> : <Send className="h-4 w-4 mr-2" />}
            Công bố kết quả
          </Button>
        )}
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Chọn cuộc đua</CardTitle>
        </CardHeader>
        <CardContent>
          <Select onValueChange={(v) => setSelectedRaceId(v)}>
            <SelectTrigger className="w-full md:w-96">
              <SelectValue placeholder="Chọn cuộc đua để xem kết quả" />
            </SelectTrigger>
            <SelectContent>
              {races.map((race: RaceResponse) => (
                <SelectItem key={race.id} value={race.id}>
                  {race.name} - {race.status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {selectedRaceId ? (
        loadingResult ? (
          <div className="flex items-center justify-center h-32">
            <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
          </div>
        ) : result ? (
          <div>
            {result.isPublished && (
              <div className="mb-4 p-3 rounded-lg bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800">
                <p className="text-sm text-green-700 dark:text-green-400">
                  ✅ Kết quả đã được công bố vào {result.publishedAt ? new Date(result.publishedAt).toLocaleString('vi-VN') : ''}
                </p>
              </div>
            )}
            <DataTable columns={columns} data={result.details ?? []} isLoading={false} emptyMessage="Chưa có kết quả" />
          </div>
        ) : (
          <div className="text-center py-12">
            <Medal className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">Chưa có kết quả cho cuộc đua này</p>
          </div>
        )
      ) : (
        <div className="text-center py-12">
          <Medal className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Chọn một cuộc đua để xem kết quả</p>
        </div>
      )}
    </div>
  );
};

export default ResultManagementPage;