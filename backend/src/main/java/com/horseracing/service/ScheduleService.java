package com.horseracing.service;

import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RaceStatus;
import com.horseracing.dto.common.SeasonStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.schedule.ScheduleRequestDTO;
import com.horseracing.dto.schedule.ScheduleResponseDTO;
import com.horseracing.entity.BanToChuc;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.MuaGiai;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.Schedule;
import com.horseracing.exception.PendingRegistrationsExistException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.BanToChucRepository;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final NguaRepository nguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final BanToChucRepository banToChucRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;

    public ScheduleResponseDTO createRace(ScheduleRequestDTO dto, String staffId, String organizerScopeId) {
        String maMuaGiai = (dto.getSeasonId() != null && !dto.getSeasonId().isBlank())
                ? dto.getSeasonId() : Schedule.MA_MUA_GIAI_DEFAULT;
        MuaGiai muaGiai = muaGiaiRepository.findById(maMuaGiai)
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "seasonId", maMuaGiai));
        assertSeasonScope(muaGiai, organizerScopeId);

        LocalDateTime raceDateTime = parseDateTime(dto.getRaceDate());
        assertRaceWithinSeason(muaGiai, raceDateTime);

        Schedule schedule = Schedule.builder()
                .maChangDua(generateMaChangDua())
                .maMuaGiai(maMuaGiai)
                .tenChangDua(dto.getName())
                .ngayThiDau(raceDateTime != null ? raceDateTime.toLocalDate() : null)
                .gioBatDau(raceDateTime != null ? raceDateTime.toLocalTime() : null)
                .diaDiem(dto.getLocation())
                .cuLy(dto.getDistance())
                .soNguaToiDa(dto.getMaxHorses())
                .soLuongToiThieu(dto.getMinHorses())
                .moTa(dto.getDescription())
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_RACE", "Race:" + saved.getMaChangDua(),
                "Tạo chặng đua mới: " + saved.getTenChangDua());

        return mapToResponseDTO(saved, muaGiai);
    }

    public ScheduleResponseDTO updateRace(String maChangDua, ScheduleRequestDTO dto, String staffId, String organizerScopeId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        assertRaceScope(schedule, organizerScopeId);
        assertRaceEditable(schedule);

        MuaGiai muaGiai = muaGiaiRepository.findById(schedule.getMaMuaGiai())
                .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "seasonId", schedule.getMaMuaGiai()));
        if (dto.getSeasonId() != null && !dto.getSeasonId().isBlank() && !dto.getSeasonId().equals(schedule.getMaMuaGiai())) {
            muaGiai = muaGiaiRepository.findById(dto.getSeasonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mùa giải", "seasonId", dto.getSeasonId()));
            assertSeasonScope(muaGiai, organizerScopeId);
            schedule.setMaMuaGiai(dto.getSeasonId());
        }

        schedule.setTenChangDua(dto.getName());
        LocalDateTime raceDateTime = parseDateTime(dto.getRaceDate());
        assertRaceWithinSeason(muaGiai, raceDateTime != null ? raceDateTime
                : LocalDateTime.of(schedule.getNgayThiDau(), schedule.getGioBatDau() != null ? schedule.getGioBatDau() : LocalTime.MIDNIGHT));
        if (raceDateTime != null) {
            schedule.setNgayThiDau(raceDateTime.toLocalDate());
            schedule.setGioBatDau(raceDateTime.toLocalTime());
        }
        schedule.setDiaDiem(dto.getLocation());
        schedule.setCuLy(dto.getDistance());
        if (dto.getMaxHorses() != null) schedule.setSoNguaToiDa(dto.getMaxHorses());
        if (dto.getMinHorses() != null) schedule.setSoLuongToiThieu(dto.getMinHorses());
        schedule.setMoTa(dto.getDescription());

        Schedule updated = scheduleRepository.save(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_RACE", "Race:" + maChangDua,
                "Cập nhật chặng đua: " + updated.getTenChangDua());

        return mapToResponseDTO(updated, muaGiai);
    }

    /**
     * Xóa cứng chỉ được phép khi chặng đua CHƯA có bất kỳ đăng ký nào (mọi trạng thái) - tránh để lại
     * đăng ký mồ côi (lỗi 8). Nếu đã có đăng ký, phải dùng cancelRace() để hủy có kiểm soát.
     */
    public void deleteRace(String maChangDua, String staffId, String organizerScopeId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        assertRaceScope(schedule, organizerScopeId);

        RaceStatus status = StatusMapper.toRaceStatus(schedule.getTrangThai());
        if (status != RaceStatus.OPEN) {
            throw new ResourceInUseException(
                    "Không thể xóa chặng đua '" + schedule.getTenChangDua() + "' vì chỉ được xóa khi ở trạng thái Mở đăng ký (hiện tại: "
                            + status + "). Hãy dùng chức năng Hủy cuộc đua.");
        }

        if (!dangKyThiDauRepository.findByMaChangDua(maChangDua).isEmpty()) {
            throw new ResourceInUseException(
                    "Không thể xóa chặng đua '" + schedule.getTenChangDua()
                            + "' vì đã có đăng ký thi đấu (kể cả đang chờ duyệt). Hãy dùng chức năng Hủy cuộc đua để hủy an toàn.");
        }

        if (!resultRepository.findByMaChangDua(maChangDua).isEmpty()) {
            throw new ResourceInUseException(
                    "Không thể xóa chặng đua '" + schedule.getTenChangDua() + "' vì đã có kết quả thi đấu.");
        }

        scheduleRepository.delete(schedule);
        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_RACE", "Race:" + maChangDua,
                "Đã xóa chặng đua: " + schedule.getTenChangDua());
    }

    /**
     * Hủy cuộc đua (lỗi 8) - dùng khi đã có đăng ký nên không thể xóa cứng. Chuyển chặng đua sang
     * CANCELLED và tự động chuyển mọi đăng ký đang PENDING/APPROVED sang CANCELLED kèm thông báo cho
     * Chủ ngựa liên quan (đăng ký đã REJECTED/CANCELLED/DISQUALIFIED trước đó giữ nguyên, không đụng tới).
     */
    public ScheduleResponseDTO cancelRace(String maChangDua, String reason, String staffId, String organizerScopeId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        assertRaceScope(schedule, organizerScopeId);

        RaceStatus status = StatusMapper.toRaceStatus(schedule.getTrangThai());
        if (status == RaceStatus.CANCELLED || status == RaceStatus.COMPLETED) {
            throw new ResourceInUseException(
                    "Không thể hủy chặng đua '" + schedule.getTenChangDua() + "' vì đang ở trạng thái " + status + ".");
        }

        String cancelReason = (reason == null || reason.isBlank()) ? "Cuộc đua đã bị Ban tổ chức hủy." : reason;
        List<DangKyThiDau> affected = dangKyThiDauRepository.findByMaChangDua(maChangDua).stream()
                .filter(r -> DangKyThiDau.TRANG_THAI_CHO_DUYET.equals(r.getTrangThai())
                        || DangKyThiDau.TRANG_THAI_DA_DUYET.equals(r.getTrangThai()))
                .collect(Collectors.toList());
        for (DangKyThiDau registration : affected) {
            registration.setTrangThai(DangKyThiDau.TRANG_THAI_DA_HUY);
            registration.setLyDo(cancelReason);
            dangKyThiDauRepository.save(registration);
            notifyOwnerRaceCancelled(registration, schedule, cancelReason);
        }

        schedule.setTrangThai(StatusMapper.toTrangThaiChangDua(RaceStatus.CANCELLED));
        Schedule updated = scheduleRepository.save(schedule);

        nhatKyHoatDongService.writeAuditLog(staffId, "CANCEL_RACE", "Race:" + maChangDua,
                "Hủy chặng đua '" + schedule.getTenChangDua() + "' (Lý do: " + cancelReason + "), chuyển "
                        + affected.size() + " đăng ký liên quan sang Đã hủy");

        MuaGiai muaGiai = muaGiaiRepository.findById(updated.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(updated, muaGiai);
    }

    private void notifyOwnerRaceCancelled(DangKyThiDau registration, Schedule schedule, String reason) {
        Ngua ngua = nguaRepository.findById(registration.getMaNgua()).orElse(null);
        if (ngua == null) return;
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null);
        if (chuNgua == null) return;
        notificationService.notify(chuNgua.getMaTK(),
                "Cuộc đua đã bị hủy",
                "Chặng đua '" + schedule.getTenChangDua() + "' đã bị Ban tổ chức hủy. Đăng ký của ngựa "
                        + ngua.getTenNgua() + " đã tự động chuyển sang Đã hủy. Lý do: " + reason,
                NotificationType.SYSTEM, "RACE", schedule.getMaChangDua());
    }

    /**
     * Chuyển trạng thái chặng đua: OPEN -> ONGOING (không còn đăng ký PENDING nào chưa xử lý, đủ số
     * đăng ký ĐÃ DUYỆT tối thiểu, và đã phân làn đầy đủ cho tất cả đăng ký đó), hoặc ONGOING -> COMPLETED.
     *
     * Chống race condition: dùng findByIdForUpdate (SELECT ... FOR UPDATE) để khóa ghi trên đúng dòng
     * ChangDua này trong suốt transaction. createRegistration cũng khóa cùng dòng này trước khi insert -
     * do đó nếu 1 đăng ký mới đang được tạo đúng lúc hàm này đang đếm PENDING, giao dịch tạo đăng ký sẽ
     * phải đợi tới khi publishRace commit/rollback xong mới được tiếp tục (và ngược lại). Vì vậy không
     * thể có tình huống "đăng ký mới chen vào" giữa lúc đếm và lúc update trạng thái.
     */
    public ScheduleResponseDTO publishRace(String maChangDua, String staffId, String organizerScopeId) {
        Schedule schedule = scheduleRepository.findByIdForUpdate(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        assertRaceScope(schedule, organizerScopeId);

        RaceStatus currentStatus = StatusMapper.toRaceStatus(schedule.getTrangThai());
        RaceStatus nextStatus;
        String action;
        if (currentStatus == RaceStatus.OPEN) {
            long pendingCount = dangKyThiDauRepository.countByMaChangDuaAndTrangThai(
                    maChangDua, DangKyThiDau.TRANG_THAI_CHO_DUYET);
            if (pendingCount > 0) {
                throw new PendingRegistrationsExistException(
                        "Còn " + pendingCount + " đăng ký chưa duyệt. Vui lòng duyệt hoặc từ chối hết trước khi chuyển sang Đang diễn ra.",
                        pendingCount);
            }
            List<DangKyThiDau> approved = dangKyThiDauRepository.findByMaChangDuaAndTrangThai(
                    maChangDua, DangKyThiDau.TRANG_THAI_DA_DUYET);
            int minRequired = schedule.getSoLuongToiThieu() != null
                    ? schedule.getSoLuongToiThieu() : Schedule.DEFAULT_SO_LUONG_TOI_THIEU;
            if (approved.size() < minRequired) {
                throw new ResourceInUseException(
                        "Chưa thể bắt đầu đua: chỉ có " + approved.size() + " đăng ký đã duyệt, cần tối thiểu "
                                + minRequired + ".");
            }
            List<DangKyThiDau> missingLane = approved.stream().filter(r -> r.getSoLan() == null).collect(Collectors.toList());
            if (!missingLane.isEmpty()) {
                throw new ResourceInUseException(
                        "Chưa thể bắt đầu đua: còn " + missingLane.size() + " đăng ký đã duyệt chưa được phân làn.");
            }
            nextStatus = RaceStatus.ONGOING;
            action = "PUBLISH_RACE";
        } else if (currentStatus == RaceStatus.ONGOING) {
            nextStatus = RaceStatus.COMPLETED;
            action = "COMPLETE_RACE";
        } else {
            throw new ResourceInUseException(
                    "Không thể chuyển trạng thái chặng đua '" + schedule.getTenChangDua() + "' từ " + currentStatus + ".");
        }

        schedule.setTrangThai(StatusMapper.toTrangThaiChangDua(nextStatus));
        Schedule updated = scheduleRepository.save(schedule);

        nhatKyHoatDongService.writeAuditLog(staffId, action, "Race:" + maChangDua,
                "Cập nhật trạng thái chặng đua: " + updated.getTenChangDua() + " → " + nextStatus);

        MuaGiai muaGiai = muaGiaiRepository.findById(updated.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(updated, muaGiai);
    }

    @Transactional(readOnly = true)
    public ScheduleResponseDTO getRaceById(String maChangDua, String organizerScopeId) {
        Schedule schedule = scheduleRepository.findById(maChangDua)
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "id", maChangDua));
        assertRaceScope(schedule, organizerScopeId);
        MuaGiai muaGiai = muaGiaiRepository.findById(schedule.getMaMuaGiai()).orElse(null);
        return mapToResponseDTO(schedule, muaGiai);
    }

    /** getRaceById nội bộ (DashboardService...) không cần kiểm tra scope - dữ liệu công khai/không nhạy cảm ở nơi gọi. */
    @Transactional(readOnly = true)
    public ScheduleResponseDTO getRaceById(String maChangDua) {
        return getRaceById(maChangDua, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponseDTO> getAllRaces(Pageable pageable, String keyword, RaceStatus status, String seasonId, String organizerScopeId) {
        List<String> organizerSeasonIds = organizerScopeId != null
                ? muaGiaiRepository.findAll((root, query, cb) -> cb.equal(root.get("maBTC"), organizerScopeId)).stream()
                        .map(MuaGiai::getMaMuaGiai).collect(java.util.stream.Collectors.toList())
                : null;

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
            if (organizerSeasonIds != null) {
                predicates.add(organizerSeasonIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("maMuaGiai").in(organizerSeasonIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(scheduleRepository.findAll(spec, pageable), schedule ->
                mapToResponseDTO(schedule, muaGiaiRepository.findById(schedule.getMaMuaGiai()).orElse(null)));
    }

    /**
     * Lỗi 7: chặn sửa BẤT KỲ thông tin gì của chặng đua (tên, thời gian, địa điểm, mùa giải, số lượng...)
     * khi đã ONGOING/COMPLETED/CANCELLED - chỉ còn OPEN mới được sửa tự do.
     */
    private void assertRaceEditable(Schedule schedule) {
        RaceStatus status = StatusMapper.toRaceStatus(schedule.getTrangThai());
        if (status != RaceStatus.OPEN) {
            throw new ResourceInUseException(
                    "Không thể sửa chặng đua '" + schedule.getTenChangDua() + "' vì đã ở trạng thái " + status
                            + " - chỉ được sửa khi còn ở trạng thái Mở đăng ký.");
        }
    }

    /** Chặn Ban tổ chức A xem/sửa/xóa chặng đua thuộc mùa giải của Ban tổ chức B. ADMIN (organizerScopeId == null) xem được tất cả. */
    private void assertSeasonScope(MuaGiai muaGiai, String organizerScopeId) {
        if (organizerScopeId != null && !organizerScopeId.equals(muaGiai.getMaBTC())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập mùa giải của Ban tổ chức khác");
        }
    }

    private void assertRaceScope(Schedule schedule, String organizerScopeId) {
        if (organizerScopeId == null) {
            return;
        }
        MuaGiai muaGiai = muaGiaiRepository.findById(schedule.getMaMuaGiai()).orElse(null);
        if (muaGiai == null || !organizerScopeId.equals(muaGiai.getMaBTC())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập chặng đua của Ban tổ chức khác");
        }
    }

    /**
     * Chặng đua phải diễn ra trong khoảng thời gian của mùa giải chứa nó, và mùa giải đó chưa kết thúc/hủy.
     */
    private void assertRaceWithinSeason(MuaGiai muaGiai, LocalDateTime raceDateTime) {
        SeasonStatus seasonStatus = StatusMapper.toSeasonStatus(muaGiai.getTrangThai());
        if (seasonStatus == SeasonStatus.CLOSED) {
            throw new ResourceInUseException(
                    "Mùa giải '" + muaGiai.getTenMuaGiai() + "' đã kết thúc/hủy, không thể tạo/sửa chặng đua thuộc mùa giải này.");
        }
        if (raceDateTime == null) {
            return;
        }
        LocalDate raceDate = raceDateTime.toLocalDate();
        if (muaGiai.getNgayBatDau() != null && raceDate.isBefore(muaGiai.getNgayBatDau())) {
            throw new IllegalArgumentException(
                    "Thời gian chặng đua (" + raceDate + ") phải từ ngày bắt đầu mùa giải '" + muaGiai.getTenMuaGiai()
                            + "' (" + muaGiai.getNgayBatDau() + ") trở đi.");
        }
        if (muaGiai.getNgayKetThuc() != null && raceDate.isAfter(muaGiai.getNgayKetThuc())) {
            throw new IllegalArgumentException(
                    "Thời gian chặng đua (" + raceDate + ") phải trước ngày kết thúc mùa giải '" + muaGiai.getTenMuaGiai()
                            + "' (" + muaGiai.getNgayKetThuc() + ").");
        }
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
                .minHorses(schedule.getSoLuongToiThieu() != null ? schedule.getSoLuongToiThieu() : Schedule.DEFAULT_SO_LUONG_TOI_THIEU)
                // Đăng ký bị từ chối/đã hủy không chiếm slot, không tính vào số lượng đã đăng ký hiển thị.
                .registeredCount(dangKyThiDauRepository.countByMaChangDuaAndTrangThaiNotIn(
                        schedule.getMaChangDua(), List.of(DangKyThiDau.TRANG_THAI_TU_CHOI, DangKyThiDau.TRANG_THAI_DA_HUY)))
                .approvedCount(dangKyThiDauRepository.countByMaChangDuaAndTrangThai(
                        schedule.getMaChangDua(), DangKyThiDau.TRANG_THAI_DA_DUYET))
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
        // Thử ISO full format "2026-06-25T14:00:00"
        try {
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception ignored) {}
        // Thử format datetime-local của HTML "2026-06-25T14:00" (không có giây)
        try {
            return LocalDateTime.parse(dateTimeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception ignored) {}
        // Thử date-only "2026-06-25"
        try {
            return LocalDate.parse(dateTimeStr).atStartOfDay();
        } catch (Exception ignored) {}
        return null;
    }
}
