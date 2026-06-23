package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.ranking.HorseRankingResponseDTO;
import com.horseracing.dto.ranking.JockeyRankingResponseDTO;
import com.horseracing.entity.Schedule;
import com.horseracing.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RankingService - tính bảng xếp hạng ngựa/jockey theo mùa giải, tính động từ KetQuaThiDua
 * (không lưu cache vào bảng BangXepHang để tránh phải đồng bộ dữ liệu).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingService {

    private final ResultRepository resultRepository;

    public PageResponse<HorseRankingResponseDTO> getHorseRanking(String seasonId, Pageable pageable) {
        String maMuaGiai = resolveSeasonId(seasonId);
        List<HorseRankingResponseDTO> all = resultRepository.findHorseRankingBySeason(maMuaGiai).stream()
                .map(row -> HorseRankingResponseDTO.builder()
                        .id((String) row[0])
                        .seasonId(maMuaGiai)
                        .horseId((String) row[0])
                        .horseName((String) row[1])
                        .horseCode((String) row[0])
                        .ownerName((String) row[2])
                        .totalPoints(toDouble(row[3]))
                        .totalRaces(toLong(row[4]))
                        .totalWins(toLong(row[5]))
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        return paginate(all, pageable);
    }

    public PageResponse<JockeyRankingResponseDTO> getJockeyRanking(String seasonId, Pageable pageable) {
        String maMuaGiai = resolveSeasonId(seasonId);
        List<JockeyRankingResponseDTO> all = resultRepository.findJockeyRankingBySeason(maMuaGiai).stream()
                .map(row -> JockeyRankingResponseDTO.builder()
                        .id((String) row[0])
                        .seasonId(maMuaGiai)
                        .jockeyId((String) row[0])
                        .jockeyName((String) row[1])
                        .ownerName((String) row[2])
                        .totalPoints(toDouble(row[3]))
                        .totalRaces(toLong(row[4]))
                        .totalWins(toLong(row[5]))
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        return paginate(all, pageable);
    }

    private String resolveSeasonId(String seasonId) {
        if (seasonId != null && !seasonId.isBlank()) return seasonId;
        return Schedule.MA_MUA_GIAI_DEFAULT;
    }

    private <T> PageResponse<T> paginate(List<T> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<T> content = start >= all.size() ? List.of() : all.subList(start, end);
        int totalPages = (int) Math.ceil((double) all.size() / pageable.getPageSize());
        return PageResponse.<T>builder()
                .content(content)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(all.size())
                .totalPages(totalPages)
                .last(pageable.getPageNumber() >= totalPages - 1)
                .build();
    }

    private Double toDouble(Object o) {
        return o == null ? 0.0 : ((Number) o).doubleValue();
    }

    private Long toLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
}
