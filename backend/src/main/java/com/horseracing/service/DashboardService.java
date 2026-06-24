package com.horseracing.service;

import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.dashboard.DashboardStatsResponseDTO;
import com.horseracing.entity.DangKyThiDau;
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

    public DashboardStatsResponseDTO getStats() {
        return DashboardStatsResponseDTO.builder()
                .totalHorses(nguaRepository.count())
                .totalJockeys(naiNguaRepository.count())
                .totalRaces(scheduleRepository.count())
                .totalSeasons(muaGiaiRepository.count())
                .pendingHorses(nguaRepository.countByTrangThai(StatusMapper.toTrangThaiNgua(com.horseracing.dto.common.HorseStatus.PENDING)))
                .pendingJockeys(naiNguaRepository.countByTrangThai(StatusMapper.toTrangThaiJockey(com.horseracing.dto.common.JockeyStatus.PENDING)))
                .pendingRegistrations(dangKyThiDauRepository.countByTrangThai(DangKyThiDau.TRANG_THAI_CHO_DUYET))
                .upcomingRaces(scheduleRepository.countByTrangThai(Schedule.TRANG_THAI_MO_DANG_KY))
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
