import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { rankingApi } from '@/api/rankingApi';
import { seasonApi } from '@/api/seasonApi';
import { queryKeys } from '@/constants/queryKeys';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { EmptyState } from '@/components/common/EmptyState';
import { Trophy, Medal } from 'lucide-react';

const LeaderboardPage = () => {
  const [seasonId, setSeasonId] = useState<string>('');

  const { data: seasonsData } = useQuery({
    queryKey: queryKeys.seasons.list({}),
    queryFn: () => seasonApi.getAll({ size: 100 }),
  });

  const params = { seasonId: (seasonId && seasonId !== 'all') ? seasonId : undefined, page: 0, size: 20 };

  const { data: horseRankings, isLoading: horseLoading } = useQuery({
    queryKey: queryKeys.rankings.horses(params),
    queryFn: () => rankingApi.getHorseRankings(params),
  });

  const { data: jockeyRankings, isLoading: jockeyLoading } = useQuery({
    queryKey: queryKeys.rankings.jockeys(params),
    queryFn: () => rankingApi.getJockeyRankings(params),
  });

  const seasons = seasonsData?.data?.data?.content;
  const horses = horseRankings?.data?.data?.content;
  const jockeys = jockeyRankings?.data?.data?.content;

  const getMedalIcon = (position: number) => {
    if (position === 1) return <span className="text-lg">🥇</span>;
    if (position === 2) return <span className="text-lg">🥈</span>;
    if (position === 3) return <span className="text-lg">🥉</span>;
    return <span className="text-sm text-muted-foreground">{position}</span>;
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-3xl font-bold text-[#1A2B4A] dark:text-white flex items-center gap-2">
          <Trophy className="h-8 w-8 text-[#D4A017]" />
          Bảng xếp hạng
        </h1>
        <Select value={seasonId} onValueChange={setSeasonId}>
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Chọn mùa giải" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {seasons?.map((s) => (
              <SelectItem key={s.id} value={s.id.toString()}>{s.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <Tabs defaultValue="horses">
        <TabsList className="mb-4">
          <TabsTrigger value="horses">🐴 Xếp hạng Ngựa</TabsTrigger>
          <TabsTrigger value="jockeys">🏇 Xếp hạng Nài</TabsTrigger>
        </TabsList>

        <TabsContent value="horses">
          <Card>
            <CardHeader><CardTitle className="flex items-center gap-2"><Medal className="h-5 w-5 text-[#D4A017]" />Top Ngựa đua</CardTitle></CardHeader>
            <CardContent>
              {horseLoading ? <LoadingSpinner /> : horses && horses.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12">#</TableHead>
                      <TableHead>Ngựa</TableHead>
                      <TableHead>Mã</TableHead>
                      <TableHead>Chủ sở hữu</TableHead>
                      <TableHead className="text-right">Điểm</TableHead>
                      <TableHead className="text-right">Số đua</TableHead>
                      <TableHead className="text-right">Thắng</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {horses.map((h, idx) => (
                      <TableRow key={h.id} className={idx < 3 ? 'bg-[#D4A017]/5' : ''}>
                        <TableCell>{getMedalIcon(idx + 1)}</TableCell>
                        <TableCell className="font-medium">{h.horseName}</TableCell>
                        <TableCell className="text-muted-foreground">{h.horseCode}</TableCell>
                        <TableCell>{h.ownerName}</TableCell>
                        <TableCell className="text-right font-bold text-[#D4A017]">{h.totalPoints}</TableCell>
                        <TableCell className="text-right">{h.totalRaces}</TableCell>
                        <TableCell className="text-right">{h.totalWins}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : <EmptyState description="Chưa có dữ liệu xếp hạng" />}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="jockeys">
          <Card>
            <CardHeader><CardTitle className="flex items-center gap-2"><Medal className="h-5 w-5 text-[#D4A017]" />Top Nài ngựa</CardTitle></CardHeader>
            <CardContent>
              {jockeyLoading ? <LoadingSpinner /> : jockeys && jockeys.length > 0 ? (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-12">#</TableHead>
                      <TableHead>Nài</TableHead>
                      <TableHead>Chủ sở hữu</TableHead>
                      <TableHead className="text-right">Điểm</TableHead>
                      <TableHead className="text-right">Số đua</TableHead>
                      <TableHead className="text-right">Thắng</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {jockeys.map((j, idx) => (
                      <TableRow key={j.id} className={idx < 3 ? 'bg-[#D4A017]/5' : ''}>
                        <TableCell>{getMedalIcon(idx + 1)}</TableCell>
                        <TableCell className="font-medium">{j.jockeyName}</TableCell>
                        <TableCell>{j.ownerName}</TableCell>
                        <TableCell className="text-right font-bold text-[#D4A017]">{j.totalPoints}</TableCell>
                        <TableCell className="text-right">{j.totalRaces}</TableCell>
                        <TableCell className="text-right">{j.totalWins}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : <EmptyState description="Chưa có dữ liệu xếp hạng" />}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default LeaderboardPage;