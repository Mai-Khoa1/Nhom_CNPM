package com.horseracing.service;

import com.horseracing.dto.dashboard.DashboardStatsResponseDTO;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.MuaGiai;
import com.horseracing.entity.Schedule;
import com.horseracing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardService - tổng hợp số liệu thống kê tổng quan của hệ thống.
 * Lỗi 10: số liệu phải đúng phạm vi theo người gọi - ORGANIZER chỉ thấy của Ban tổ chức mình,
 * ADMIN thấy toàn hệ thống (không lọc), khán giả/role khác thấy tổng mùa giải/cuộc đua toàn hệ
 * thống nhưng số ngựa/nài phải khớp với danh sách công khai (chỉ tính ngựa/nài đã có đăng ký APPROVED).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;
    private final ScheduleRepository scheduleRepository;
    private final MuaGiaiRepository muaGiaiRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final ScheduleService scheduleService;

    public DashboardStatsResponseDTO getStats(String organizerScopeId, boolean isAdmin) {
        if (organizerScopeId != null) {
            return getOrganizerStats(organizerScopeId);
        }
        if (isAdmin) {
            return getGlobalStats();
        }
        return getPublicStats();
    }

    /** ADMIN - toàn quyền, số liệu không lọc (giữ nguyên hành vi trước khi sửa lỗi 10). */
    private DashboardStatsResponseDTO getGlobalStats() {
        return DashboardStatsResponseDTO.builder()
                .totalHorses(nguaRepository.count())
                .totalJockeys(naiNguaRepository.count())
                .totalRaces(scheduleRepository.count())
                .totalSeasons(muaGiaiRepository.count())
                .pendingRegistrations(dangKyThiDauRepository.countByTrangThai(DangKyThiDau.TRANG_THAI_CHO_DUYET))
                .upcomingRaces(scheduleRepository.countByTrangThai(Schedule.TRANG_THAI_MO_DANG_KY))
                .build();
    }

    /**
     * Khán giả/trang công khai - mùa giải & cuộc đua vẫn là tổng toàn hệ thống (số liệu tổng quan công
     * khai, đúng như thiết kế ban đầu), nhưng số ngựa/nài phải khớp với danh sách công khai thực tế
     * hiển thị (chỉ tính ngựa/nài đã có ít nhất 1 đăng ký APPROVED), không phải tổng số hồ sơ đã tạo.
     */
    private DashboardStatsResponseDTO getPublicStats() {
        return DashboardStatsResponseDTO.builder()
                .totalHorses(dangKyThiDauRepository.countDistinctApprovedHorses())
                .totalJockeys(dangKyThiDauRepository.countDistinctApprovedJockeys())
                .totalRaces(scheduleRepository.count())
                .totalSeasons(muaGiaiRepository.count())
                .pendingRegistrations(dangKyThiDauRepository.countByTrangThai(DangKyThiDau.TRANG_THAI_CHO_DUYET))
                .upcomingRaces(scheduleRepository.countByTrangThai(Schedule.TRANG_THAI_MO_DANG_KY))
                .build();
    }

    /**
     * Ban tổ chức - mọi số liệu chỉ tính trong phạm vi của đúng Ban tổ chức đang đăng nhập (multi-tenancy).
     * Số ngựa/nài KHÔNG phải tổng số hồ sơ chủ ngựa sở hữu, mà là số ngựa/nài đã có đăng ký APPROVED
     * tại chính Ban tổ chức này.
     */
    private DashboardStatsResponseDTO getOrganizerStats(String maBTC) {
        List<String> seasonIds = muaGiaiRepository.findAll((root, query, cb) -> cb.equal(root.get("maBTC"), maBTC))
                .stream().map(MuaGiai::getMaMuaGiai).collect(Collectors.toList());
        long totalRaces = seasonIds.isEmpty() ? 0 : scheduleRepository.countByMaMuaGiaiIn(seasonIds);
        long upcomingRaces = seasonIds.isEmpty() ? 0
                : scheduleRepository.countByMaMuaGiaiInAndTrangThai(seasonIds, Schedule.TRANG_THAI_MO_DANG_KY);

        return DashboardStatsResponseDTO.builder()
                .totalHorses(dangKyThiDauRepository.countDistinctApprovedHorsesByOrganizer(maBTC))
                .totalJockeys(dangKyThiDauRepository.countDistinctApprovedJockeysByOrganizer(maBTC))
                .totalRaces(totalRaces)
                .totalSeasons(muaGiaiRepository.countByMaBTC(maBTC))
                .pendingRegistrations(dangKyThiDauRepository.countPendingByOrganizer(maBTC))
                .upcomingRaces(upcomingRaces)
                .build();
    }

    public List<com.horseracing.dto.schedule.ScheduleResponseDTO> getUpcomingRaces() {
        Pageable top5 = PageRequest.of(0, 5, Sort.by("ngayThiDau").ascending());
        return scheduleRepository.findAll(top5).getContent().stream()
                .filter(s -> Schedule.TRANG_THAI_MO_DANG_KY.equals(s.getTrangThai()))
                .map(s -> scheduleService.getRaceById(s.getMaChangDua()))
                .collect(Collectors.toList());
    }
}
