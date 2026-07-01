import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { raceApi } from '@/api/raceApi';
import { laneApi } from '@/api/laneApi';
import { queryKeys } from '@/constants/queryKeys';
import { RaceResponse } from '@/types/race';
import { RegistrationResponse } from '@/types/registration';
import { RegistrationStatus } from '@/types/enums';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { handleApiError } from '@/utils/apiHelpers';
import { Layers, Loader2, Save, Trash2 } from 'lucide-react';
import { toast } from 'sonner';

interface LaneInputRow {
  registrationId: string;
  horseName: string;
  horseCode: string;
  jockeyName: string;
  currentLane?: number;
  inputLane: string;
}

const LaneManagementPage = () => {
  const queryClient = useQueryClient();
  const [selectedRaceId, setSelectedRaceId] = useState<string | null>(null);
  const [laneInputs, setLaneInputs] = useState<Record<string, string>>({});

  const { data: racesData } = useQuery({
    queryKey: queryKeys.races.list({ size: 100 }),
    queryFn: () => raceApi.getAll({ size: 100 }),
  });

  const { data: registrationsData, isLoading: loadingRegs } = useQuery({
    queryKey: queryKeys.races.registrations(selectedRaceId!),
    queryFn: () => raceApi.getRegistrations(selectedRaceId!, { size: 50, status: RegistrationStatus.APPROVED }),
    enabled: !!selectedRaceId,
  });

  const invalidateLanes = () => {
    queryClient.invalidateQueries({ queryKey: queryKeys.races.registrations(selectedRaceId!) });
    queryClient.invalidateQueries({ queryKey: queryKeys.races.lanes(selectedRaceId!) });
  };

  const assignMutation = useMutation({
    mutationFn: ({ registrationId, laneNumber }: { registrationId: string; laneNumber: number }) =>
      laneApi.assign({ registrationId, laneNumber }),
    onSuccess: (_, vars) => {
      toast.success(`Đã gán làn ${vars.laneNumber}`);
      invalidateLanes();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ registrationId, laneNumber }: { registrationId: string; laneNumber: number }) =>
      laneApi.update(registrationId, { registrationId, laneNumber }),
    onSuccess: (_, vars) => {
      toast.success(`Đã cập nhật làn ${vars.laneNumber}`);
      invalidateLanes();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const removeMutation = useMutation({
    mutationFn: (registrationId: string) => laneApi.delete(registrationId),
    onSuccess: () => {
      toast.success('Đã xóa gán làn');
      invalidateLanes();
    },
    onError: (error) => toast.error(handleApiError(error)),
  });

  const races: RaceResponse[] = racesData?.data?.data?.content ?? [];
  const registrations: RegistrationResponse[] = registrationsData?.data?.data?.content ?? [];
  const approvedRegs = registrations.filter((r) => r.status === RegistrationStatus.APPROVED);

  const handleRaceSelect = (raceId: string) => {
    setSelectedRaceId(raceId);
    setLaneInputs({});
  };

  const getLaneInput = (reg: RegistrationResponse) =>
    laneInputs[reg.id] !== undefined ? laneInputs[reg.id] : (reg.laneNumber?.toString() ?? '');

  const setLaneInput = (regId: string, value: string) =>
    setLaneInputs((prev) => ({ ...prev, [regId]: value }));

  const handleSaveLane = (reg: RegistrationResponse) => {
    const inputVal = laneInputs[reg.id];
    if (inputVal === undefined || inputVal === '') {
      toast.error('Nhập số làn trước khi lưu');
      return;
    }
    const laneNum = parseInt(inputVal, 10);
    if (isNaN(laneNum) || laneNum < 1 || laneNum > 30) {
      toast.error('Số làn phải từ 1 đến 30');
      return;
    }
    if (reg.laneNumber != null) {
      updateMutation.mutate({ registrationId: reg.id, laneNumber: laneNum });
    } else {
      assignMutation.mutate({ registrationId: reg.id, laneNumber: laneNum });
    }
  };

  const handleRemoveLane = (reg: RegistrationResponse) => {
    removeMutation.mutate(reg.id);
    setLaneInputs((prev) => ({ ...prev, [reg.id]: '' }));
  };

  const isMutating = assignMutation.isPending || updateMutation.isPending || removeMutation.isPending;

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
          <Select onValueChange={handleRaceSelect} value={selectedRaceId ?? ''}>
            <SelectTrigger className="w-full md:w-96">
              <SelectValue placeholder="Chọn cuộc đua để phân làn" />
            </SelectTrigger>
            <SelectContent>
              {races.map((race) => (
                <SelectItem key={race.id} value={race.id}>
                  {race.name} — {race.status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </CardContent>
      </Card>

      {selectedRaceId ? (
        loadingRegs ? (
          <div className="flex items-center justify-center h-32">
            <Loader2 className="h-6 w-6 animate-spin text-[#D4A017]" />
          </div>
        ) : approvedRegs.length === 0 ? (
          <div className="text-center py-12">
            <Layers className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground">Chưa có đăng ký nào được duyệt cho cuộc đua này</p>
          </div>
        ) : (
          <div>
            <p className="text-sm text-muted-foreground mb-3">
              Nhập số làn cho từng đăng ký được duyệt rồi nhấn <strong>Lưu</strong>.
              Để xóa làn đã gán, nhấn nút <Trash2 className="inline h-3 w-3" />.
            </p>
            <div className="rounded-md border overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-muted">
                  <tr>
                    <th className="text-left p-3 font-medium">Ngựa</th>
                    <th className="text-left p-3 font-medium">Nài</th>
                    <th className="text-left p-3 font-medium w-32">Làn hiện tại</th>
                    <th className="text-left p-3 font-medium w-36">Làn mới</th>
                    <th className="text-left p-3 font-medium w-32">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  {approvedRegs.map((reg) => (
                    <tr key={reg.id} className="border-t">
                      <td className="p-3">
                        <span className="font-medium">{reg.horseName}</span>
                        <span className="text-muted-foreground ml-1 text-xs">({reg.horseCode})</span>
                      </td>
                      <td className="p-3">{reg.jockeyName}</td>
                      <td className="p-3">
                        {reg.laneNumber != null ? (
                          <span className="font-bold text-[#D4A017]">Làn {reg.laneNumber}</span>
                        ) : (
                          <span className="text-muted-foreground">Chưa gán</span>
                        )}
                      </td>
                      <td className="p-3">
                        <Input
                          type="number"
                          min={1}
                          max={30}
                          placeholder="VD: 3"
                          value={getLaneInput(reg)}
                          onChange={(e) => setLaneInput(reg.id, e.target.value)}
                          className="w-24"
                          disabled={isMutating}
                        />
                      </td>
                      <td className="p-3">
                        <div className="flex gap-1">
                          <Button
                            size="sm"
                            className="bg-[#D4A017] hover:bg-[#C8940A] text-white h-8 px-3"
                            onClick={() => handleSaveLane(reg)}
                            disabled={isMutating}
                          >
                            {(assignMutation.isPending || updateMutation.isPending) ? (
                              <Loader2 className="h-3 w-3 animate-spin" />
                            ) : (
                              <Save className="h-3 w-3" />
                            )}
                          </Button>
                          {reg.laneNumber != null && (
                            <Button
                              size="sm"
                              variant="outline"
                              className="h-8 px-3 text-red-500 hover:text-red-600"
                              onClick={() => handleRemoveLane(reg)}
                              disabled={isMutating}
                              title="Xóa làn đua"
                            >
                              {removeMutation.isPending ? (
                                <Loader2 className="h-3 w-3 animate-spin" />
                              ) : (
                                <Trash2 className="h-3 w-3" />
                              )}
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      ) : (
        <div className="text-center py-12">
          <Layers className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <p className="text-muted-foreground">Chọn một cuộc đua để quản lý phân làn</p>
        </div>
      )}
    </div>
  );
};

export default LaneManagementPage;
