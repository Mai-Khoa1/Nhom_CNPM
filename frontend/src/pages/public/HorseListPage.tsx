import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { horseApi } from '@/api/horseApi';
import { queryKeys } from '@/constants/queryKeys';
import { Card, CardContent } from '@/components/ui/card';
import { SearchBar } from '@/components/common/SearchBar';
import { PaginationControl } from '@/components/common/PaginationControl';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { EmptyState } from '@/components/common/EmptyState';
import { useDebounce } from '@/hooks/useDebounce';
import { resolveApiUrl } from '@/utils/resolveApiUrl';

const HorseListPage = () => {
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState('');
  const debouncedKeyword = useDebounce(keyword);

  const params = {
    page,
    size: 12,
    keyword: debouncedKeyword || undefined,
  };

  const { data, isLoading } = useQuery({
    queryKey: queryKeys.horses.list(params),
    queryFn: () => horseApi.getAll(params),
  });

  const horses = data?.data?.data?.content;
  const pageInfo = data?.data?.data;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-[#1A2B4A] dark:text-white mb-6">Danh sách ngựa đua</h1>

      <div className="mb-6 max-w-md">
        <SearchBar value={keyword} onChange={setKeyword} placeholder="Tìm kiếm ngựa theo tên..." />
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-12" text="Đang tải danh sách ngựa..." />
      ) : horses && horses.length > 0 ? (
        <>
          <div className="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {horses.map((horse) => (
              <Link key={horse.id} to={`/horses/${horse.id}`}>
                <Card className="hover:shadow-lg transition-shadow cursor-pointer h-full">
                  <div className="aspect-square bg-muted overflow-hidden rounded-t-xl flex items-center justify-center">
                    {horse.avatarUrl ? (
                      <img src={resolveApiUrl(horse.avatarUrl)} alt={horse.name} className="h-full w-full object-cover" />
                    ) : (
                      <span className="text-5xl">🐴</span>
                    )}
                  </div>
                  <CardContent className="p-4">
                    <p className="font-semibold truncate">{horse.name}</p>
                    <p className="text-sm text-muted-foreground truncate">{horse.breed}</p>
                    <p className="text-xs text-muted-foreground mt-1">Chủ: {horse.ownerName}</p>
                  </CardContent>
                </Card>
              </Link>
            ))}
          </div>
          {pageInfo && (
            <PaginationControl
              page={pageInfo.page}
              totalPages={pageInfo.totalPages}
              totalElements={pageInfo.totalElements}
              onPageChange={setPage}
            />
          )}
        </>
      ) : (
        <EmptyState title="Chưa có ngựa nào đang thi đấu" description="Thử thay đổi từ khóa tìm kiếm" />
      )}
    </div>
  );
};

export default HorseListPage;
