import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceResponse } from '@/types/race';
import { LaneResponse } from '@/types/lane';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { Layers, Loader2 } from 'lucide-react';

const LaneManagementPage = () => {
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null);

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ size: 100 }),
    queryFn: () => raceApi.getAll({ size: 100 }),
  });

  const { data: lanesData, isLoading: loadingLanes } = useQuery({
    queryKey: ['lanes', selectedRaceId],
    queryFn: () => raceApi.getLanes(selectedRaceId!),
    enabled: !!selectedRaceId,
  });

  const races = racesData?.data?.data?.content ?? [];
  const lanes = lanesData?.data?.data ?? [];

  const columns: Column<LaneResponse>[] = [
    { key: 'laneNumber', header: 'Làn số', render: (l) => <span className="font-bold text-lg">{l.laneNumber}</span> },
    { key: 'horseName', header: 'Ngựa', render: (l) => <span className="font-medium">{l.horseName}</span> },
    { key: 'jockeyName', header: 'Nài' },
    { key: 'assignedAt', header: 'Ngày phân làn', render: (l) => new Date(l.assignedAt).toLocaleDateString('vi-VN') },
  ];

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý làn đua</h1>
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Chọn cuộc đua</CardTitle>
        </CardHeader>
        <CardContent>
          <Select onValueChange={(v) => setSelectedRaceId(v)}>
            <SelectTrigger className="w-full md:w-96">
              <SelectValue placeholder="Chọn cuộc đua để xem phân làn" />
            </SelectTrigger>
            <SelectContent>
              {races.map((race: RaceResponse) => (
                <SelectItem key={race.id} value={race.id}>
                  {race.name} - {race.location} ({race.raceDate})
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {selectedRaceId ? (
        loadingLanes ? (
          <div className="flex items-center justify-center h-32">
            <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
          </div>
        ) : (
          <DataTable columns={columns} data={lanes} isLoading={false} emptyMessage="Chưa có phân làn cho cuộc đua này" />
        )
      ) : (
        <div className="text-center py-12">
          <Layers className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Chọn một cuộc đua để xem thông tin phân làn</p>
        </div>
      )}
    </div>
  );
};

export default LaneManagementPage;