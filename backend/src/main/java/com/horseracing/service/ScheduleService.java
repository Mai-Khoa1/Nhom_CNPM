package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.schedule.ScheduleRequestDTO;
import com.horseracing.dto.schedule.ScheduleResponseDTO;
import com.horseracing.entity.MuaGiai;
import com.horseracing.entity.Schedule;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ScheduleService - quản lý lịch đua / chặng đua (bảng ChangDua). Đối ngoại gọi là "Race".
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final MuaGiaiRepository muaGiaiRepository;
    private final ResultRepository resultRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    public ScheduleResponseDTO createRace(ScheduleRequestDTO dto, String staffId) {
        String maMuaGiai = (dto.getSeasonId() != null && !dto.getSeasonId().isBlank())
                ? dto.getSeasonId() : Schedule.MA_MUA_GIAI_DEFAULT;
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "seasonId", maMuaGiai));

        LocalDateTime raceDateTime = parseDateTime(dto.getRaceDate());

        Schedule schedule = Schedule.builder()
                .maChangDua(generateMaChangDua())
                .maMuaGiai(maMuaGiai)
                .tenChangDua(dto.getName())
                .ngayThiDau(raceDateTime != null ? raceDateTime.toLocalDate() : null)
                .gioBatDau(raceDateTime != null ? raceDateTime.toLocalTime() : null)
                .diaDiem(dto.getLocation())
                .cuLy(dto.getDistance())
                .soNguaToiDa(dto.getMaxHorses())
                .moTa(dto.getDescription())
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_RACE", "Race:" + saved.getMaChangDua(),
                "Tạo chặng đua mới: " + saved.getTenChangDua());

        return mapToResponseDTO(saved, muaGiai);
    }

    public ScheduleResponseDTO updateRace(String maChangDua, ScheduleRequestDTO dto, String staffId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));

        if (dto.getSeasonId() != null && !dto.getSeasonId().isBlank()) {
            muaGiaiRepository.findById(dto.getSeasonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "seasonId", dto.getSeasonId()));
            schedule.setMaMuaGiai(dto.getSeasonId());
        }

        schedule.setTenChangDua(dto.getName());
        LocalDateTime raceDateTime = parseDateTime(dto.getRaceDate());
        if (raceDateTime != null) {
            schedule.setNgayThiDau(raceDateTime.toLocalDate());
            schedule.setGioBatDau(raceDateTime.toLocalTime());
        }
        schedule.setDiaDiem(dto.getLocation());
        schedule.setCuLy(dto.getDistance());
        if (dto.getMaxHorses() != null) schedule.setSoNguaToiDa(dto.getMaxHorses());
        schedule.setMoTa(dto.getDescription());

        Schedule updated = scheduleRepository.save(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_RACE", "Race:" + maChangDua,
                "Cập nhật chặng đua: " + updated.getTenChangDua());

        MuaGiai muaGiai = muaGiaiRepository.findById(updated.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(updated, muaGiai);
    }

    public void deleteRace(String maChangDua, String staffId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));

        if (!resultRepository.findByMaChangDua(maChangDua).isEmpty()) {
            throw new ResourceInUseException(
                    "Không thể xóa chặng đua '" + schedule.getTenChangDua() + "' vì đã có kết quả thi đấu.");
        }

        scheduleRepository.delete(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_RACE", "Race:" + maChangDua,
                "Đã xóa chặng đua: " + schedule.getTenChangDua());
    }

    public ScheduleResponseDTO publishRace(String maChangDua, String staffId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));

        schedule.setTrangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.ONGOING));
        Schedule updated = scheduleRepository.save(schedule);

        nhatKyHoatDongService.writeAuditLog(staffId, "PUBLISH_RACE", "Race:" + maChangDua,
                "Công bố/mở chặng đua: " + updated.getTenChangDua());

        MuaGiai muaGiai = muaGiaiRepository.findById(updated.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(updated, muaGiai);
    }

    @Transactional(readOnly = true)
    public ScheduleResponseDTO getRaceById(String maChangDua) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        MuaGiai muaGiai = muaGiaiRepository.findById(schedule.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(schedule, muaGiai);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponseDTO> getAllRaces(Pageable pageable, String keyword, RaceStatus status, String seasonId) {
        Specification<Schedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tenChangDua")), "%" + keyword.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), StatusMapper.toTrangThaiChangDua(status)));
            }
            if (seasonId != null && !seasonId.isBlank()) {
                predicates.add(cb.equal(root.get("maMuaGiai"), seasonId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(scheduleRepository.findAll(spec, pageable), schedule ->
                mapToResponseDTO(schedule, muaGiaiRepository.findById(schedule.getMaMuaGiai()).orElse(null)));
    }

    private ScheduleResponseDTO mapToResponseDTO(Schedule schedule, MuaGiai muaGiai) {
        LocalDateTime raceDateTime = schedule.getNgayThiDau() != null
                ? LocalDateTime.of(schedule.getNgayThiDau(), schedule.getGioBatDau() != null ? schedule.getGioBatDau() : LocalTime.MIDNIGHT)
                : null;

        return ScheduleResponseDTO.builder()
                .id(schedule.getMaChangDua())
                .seasonId(schedule.getMaMuaGiai())
                .seasonName(muaGiai != null ? muaGiai.getTenMuaGiai() : null)
                .name(schedule.getTenChangDua())
                .raceDate(raceDateTime)
                .location(schedule.getDiaDiem())
                .distance(schedule.getCuLy())
                .maxHorses(schedule.getSoNguaToiDa())
                .registeredCount(dangKyThiDauRepository.countByMaChangDua(schedule.getMaChangDua()))
                .status(StatusMapper.toRaceStatus(schedule.getTrangThai()))
                .description(schedule.getMoTa())
                .createdAt(schedule.getNgayTao())
                .build();
    }

    private String generateMaChangDua() {
        return "CD" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return LocalDate.parse(dateTimeStr).atStartOfDay();
        }
    }
}
