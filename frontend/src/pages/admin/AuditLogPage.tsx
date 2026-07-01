import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '@/api/auditApi';
import { AuditLogResponse } from '@/types/audit';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { formatDateTime } from '@/utils/formatDate';

const ACTION_OPTIONS = [
  'ALL',
  'LOGIN', 'LOGOUT',
  'CREATE_RACE', 'UPDATE_RACE', 'DELETE_RACE', 'PUBLISH_RACE', 'COMPLETE_RACE',
  'CREATE_SEASON', 'UPDATE_SEASON', 'DELETE_SEASON',
  'APPROVE_HORSE', 'REJECT_HORSE', 'DISQUALIFY_HORSE',
  'APPROVE_JOCKEY', 'REJECT_JOCKEY',
  'APPROVE_REGISTRATION', 'REJECT_REGISTRATION',
  'SUBMIT_RESULTS', 'PUBLISH_RESULTS',
  'ASSIGN_LANE', 'UPDATE_LANE', 'REMOVE_LANE',
  'CREATE_USER', 'UPDATE_USER', 'DELETE_USER', 'LOCK_USER', 'UNLOCK_USER', 'CHANGE_ROLE',
];

const AuditLogPage = () => {
  const [actionFilter, setActionFilter] = useState<string>('ALL');

  const { data, isLoading } = useQuery({
    queryKey: ['audit-logs', actionFilter],
    queryFn: () => auditApi.getAll({ action: actionFilter !== 'ALL' ? actionFilter : undefined }),
  });

  const logs: AuditLogResponse[] = data?.data?.data ?? [];

  const columns: Column<AuditLogResponse>[] = [
    { key: 'thoiGian', header: 'Thời gian', render: (l) => formatDateTime(l.thoiGian) },
    { key: 'maTK', header: 'Tài khoản', render: (l) => l.maTK ?? '-' },
    {
      key: 'loaiHanhDong', header: 'Hành động',
      render: (l) => (
        <span className="font-mono text-xs bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">
          {l.loaiHanhDong}
        </span>
      ),
    },
    { key: 'doiTuongTacDong', header: 'Đối tượng', render: (l) => l.doiTuongTacDong ?? '-' },
    { key: 'moTa', header: 'Mô tả', render: (l) => l.moTa ?? '-' },
    { key: 'diaChiIP', header: 'IP', render: (l) => l.diaChiIP ?? '-' },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Audit Log</h1>

      <div className="mb-4">
        <Select value={actionFilter} onValueChange={setActionFilter}>
          <SelectTrigger className="w-[240px]">
            <SelectValue placeholder="Lọc hành động" />
          </SelectTrigger>
          <SelectContent>
            {ACTION_OPTIONS.map((a) => (
              <SelectItem key={a} value={a}>{a === 'ALL' ? 'Tất cả' : a}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={logs} isLoading={isLoading} emptyMessage="Không có nhật ký nào" />
    </div>
  );
};

export default AuditLogPage;
