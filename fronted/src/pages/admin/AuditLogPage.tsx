import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { auditApi } from '@/api/auditApi';
import { queryKeys } from '@/constants/queryKeys';
import { AuditLogResponse } from '@/types/audit';
import { AuditAction } from '@/types/enums';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DataTable, Column } from '@/components/common/DataTable';
import { PaginationControl } from '@/components/common/PaginationControl';
import { formatDateTime } from '@/utils/formatDate';

const AuditLogPage = () => {
  const [page, setPage] = useState(0);
  const [actionFilter, setActionFilter] = useState<string>('ALL');

  const params = {
    page,
    size: 20,
    action: actionFilter !== 'ALL' ? actionFilter as AuditAction : undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.auditLogs.list(params),
    queryFn: () => auditApi.getAll(params),
  });

  const logs = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  const columns: Column<AuditLogResponse>[] = [
    { key: 'createdAt', header: 'Thời gian', render: (l) => formatDateTime(l.createdAt) },
    { key: 'username', header: 'Người dùng', render: (l) => l.username ?? '-' },
    { key: 'action', header: 'Hành động', render: (l) => <span className="font-mono text-xs bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">{l.action}</span> },
    { key: 'targetType', header: 'Đối tượng', render: (l) => l.targetType ?? '-' },
    { key: 'description', header: 'Mô tả', render: (l) => l.description ?? '-' },
    { key: 'ipAddress', header: 'IP', render: (l) => l.ipAddress ?? '-' },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Audit Log</h1>

      <div className="mb-4">
        <Select value={actionFilter} onValueChange={setActionFilter}>
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Lọc hành động" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Tất cả</SelectItem>
            {Object.values(AuditAction).map((a) => (
              <SelectItem key={a} value={a}>{a}</SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <DataTable columns={columns} data={logs ?? []} isLoading={isLoading} />

      {pageInfo && (
        <PaginationControl page={pageInfo.page} totalPages={pageInfo.totalPages} totalElements={pageInfo.totalElements} onPageChange={setPage} />
      )}
    </div>
  );
};

export default AuditLogPage;