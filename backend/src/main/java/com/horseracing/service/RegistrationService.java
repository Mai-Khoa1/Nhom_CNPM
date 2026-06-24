package com.horseracing.service;

import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.RegistrationStatus;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.lane.LaneRequestDTO;
import com.horseracing.dto.lane.LaneResponseDTO;
import com.horseracing.dto.registration.RegistrationRequestDTO;
import com.horseracing.dto.registration.RegistrationResponseDTO;
import com.horseracing.entity.*;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RegistrationService - quản lý đăng ký thi đấu (DangKyThiDau) và gán làn đua (Lane, dùng chung entity).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationService {

    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final ScheduleRepository scheduleRepository;
    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;

    public RegistrationResponseDTO createRegistration(RegistrationRequestDTO dto, String ownerMaTK, String staffId) {
        Schedule schedule = scheduleRepository.findById(dto.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Chặng đua", "raceId", dto.getRaceId()));
        Ngua ngua = nguaRepository.findById(dto.getHorseId())
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "horseId", dto.getHorseId()));
        NaiNgua naiNgua = naiNguaRepository.findById(dto.getJockeyId())
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "jockeyId", dto.getJockeyId()));

        if (dangKyThiDauRepository.existsByMaChangDuaAndMaNgua(dto.getRaceId(), dto.getHorseId())) {
            throw new DuplicateResourceException("Ngựa '" + ngua.getTenNgua() + "' đã được đăng ký trong chặng đua này.");
        }
        if (dangKyThiDauRepository.existsByMaChangDuaAndMaNaiNgua(dto.getRaceId(), dto.getJockeyId())) {
            throw new DuplicateResourceException("Jockey '" + naiNgua.getHoTen() + "' đã được đăng ký trong chặng đua này.");
        }
        if (schedule.getSoNguaToiDa() != null
                && dangKyThiDauRepository.countByMaChangDua(dto.getRaceId()) >= schedule.getSoNguaToiDa()) {
            throw new ResourceInUseException("Chặng đua '" + schedule.getTenChangDua() + "' đã đủ số lượng đăng ký tối đa.");
        }

        DangKyThiDau registration = DangKyThiDau.builder()
                .maDangKy(generateMaDangKy())
                .maChangDua(dto.getRaceId())
                .maNgua(dto.getHorseId())
                .maNaiNgua(dto.getJockeyId())
                .build();

        DangKyThiDau saved = dangKyThiDauRepository.save(registration);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_REGISTRATION", "Registration:" + saved.getMaDangKy(),
                "Đăng ký thi đấu: ngựa " + ngua.getTenNgua() + " - jockey " + naiNgua.getHoTen());

        notificationService.notifyAdmins(
                "Đăng ký thi đấu mới chờ duyệt",
                "Ngựa " + ngua.getTenNgua() + " (jockey " + naiNgua.getHoTen() + ") đã đăng ký thi đấu chặng đua '"
                        + schedule.getTenChangDua() + "', cần duyệt.",
                NotificationType.SYSTEM, "REGISTRATION", saved.getMaDangKy());

        return mapToResponseDTO(saved, schedule, ngua, naiNgua);
    }

    public RegistrationResponseDTO approveRegistration(String maDangKy, String staffId) {
        return changeStatus(maDangKy, RegistrationStatus.APPROVED, null, staffId);
    }

    public RegistrationResponseDTO rejectRegistration(String maDangKy, String reason, String staffId) {
        return changeStatus(maDangKy, RegistrationStatus.REJECTED, reason, staffId);
    }

    private RegistrationResponseDTO changeStatus(String maDangKy, RegistrationStatus status, String reason, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));

        registration.setTrangThai(status == RegistrationStatus.APPROVED
                ? DangKyThiDau.TRANG_THAI_DA_DUYET : DangKyThiDau.TRANG_THAI_TU_CHOI);
        if (reason != null) {
            registration.setLyDoTuChoi(reason);
        }
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId,
                status == RegistrationStatus.APPROVED ? "APPROVE_REGISTRATION" : "REJECT_REGISTRATION",
                "Registration:" + maDangKy, "Cập nhật trạng thái đăng ký -> " + status);

        RegistrationResponseDTO responseDTO = mapToFullResponseDTO(updated);
        boolean approved = status == RegistrationStatus.APPROVED;
        notificationService.notify(responseDTO.getOwnerId(),
                approved ? "Đăng ký thi đấu được duyệt" : "Đăng ký thi đấu bị từ chối",
                "Đăng ký cho ngựa " + responseDTO.getHorseName() + " tại chặng đua " + responseDTO.getRaceName()
                        + (approved ? " đã được duyệt." : " đã bị từ chối" + (reason != null ? ": " + reason : ".")),
                approved ? NotificationType.APPROVAL : NotificationType.REJECTION,
                "REGISTRATION", maDangKy);

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public RegistrationResponseDTO getRegistrationById(String maDangKy) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", maDangKy));
        return mapToFullResponseDTO(registration);
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getAllRegistrations(Pageable pageable, String raceId, RegistrationStatus status) {
        Specification<DangKyThiDau> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (raceId != null && !raceId.isBlank()) {
                predicates.add(cb.equal(root.get("maChangDua"), raceId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), toTrangThai(status)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(dangKyThiDauRepository.findAll(spec, pageable), this::mapToFullResponseDTO);
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getMyRegistrations(String ownerMaTK, Pageable pageable) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        List<String> horseIds = nguaRepository.findByMaChuNgua(chuNgua.getMaChuNgua()).stream()
                .map(Ngua::getMaNgua).collect(Collectors.toList());

        if (horseIds.isEmpty()) {
            return PageResponse.<RegistrationResponseDTO>builder()
                    .content(List.of()).page(pageable.getPageNumber()).size(pageable.getPageSize())
                    .totalElements(0).totalPages(0).last(true).build();
        }

        Specification<DangKyThiDau> spec = (root, query, cb) -> root.get("maNgua").in(horseIds);
        return PageResponse.of(dangKyThiDauRepository.findAll(spec, pageable), this::mapToFullResponseDTO);
    }

    // ----- Lane assignment (dùng chung entity DangKyThiDau) -----

    public LaneResponseDTO assignLane(LaneRequestDTO dto, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(dto.getRegistrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "id", dto.getRegistrationId()));

        registration.setSoLan(dto.getLaneNumber());
        registration.setNgayGanLan(LocalDateTime.now());
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "ASSIGN_LANE", "Registration:" + updated.getMaDangKy(),
                "Gán làn đua số " + dto.getLaneNumber());

        return mapToLaneResponseDTO(updated);
    }

    public LaneResponseDTO updateLane(String maDangKy, LaneRequestDTO dto, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Làn đua", "id", maDangKy));

        registration.setSoLan(dto.getLaneNumber());
        registration.setNgayGanLan(LocalDateTime.now());
        DangKyThiDau updated = dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_LANE", "Registration:" + maDangKy,
                "Cập nhật làn đua số " + dto.getLaneNumber());

        return mapToLaneResponseDTO(updated);
    }

    public void removeLane(String maDangKy, String staffId) {
        DangKyThiDau registration = dangKyThiDauRepository.findById(maDangKy)
                .orElseThrow(() -> new ResourceNotFoundException("Làn đua", "id", maDangKy));

        registration.setSoLan(null);
        registration.setNgayGanLan(null);
        dangKyThiDauRepository.save(registration);

        nhatKyHoatDongService.writeAuditLog(staffId, "REMOVE_LANE", "Registration:" + maDangKy, "Bỏ gán làn đua");
    }

    @Transactional(readOnly = true)
    public List<LaneResponseDTO> getLanesByRace(String maChangDua) {
        return dangKyThiDauRepository.findByMaChangDua(maChangDua).stream()
                .filter(r -> r.getSoLan() != null)
                .map(this::mapToLaneResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponseDTO> getRegistrationsByRace(String maChangDua, Pageable pageable) {
        return PageResponse.of(dangKyThiDauRepository.findByMaChangDua(maChangDua, pageable), this::mapToFullResponseDTO);
    }

    // ----- Helper -----

    private RegistrationResponseDTO mapToFullResponseDTO(DangKyThiDau registration) {
        Schedule schedule = scheduleRepository.findById(registration.getMaChangDua()).orElse(null);
        Ngua ngua = nguaRepository.findById(registration.getMaNgua()).orElse(null);
        NaiNgua naiNgua = naiNguaRepository.findById(registration.getMaNaiNgua()).orElse(null);
        return mapToResponseDTO(registration, schedule, ngua, naiNgua);
    }

    private RegistrationResponseDTO mapToResponseDTO(DangKyThiDau registration, Schedule schedule, Ngua ngua, NaiNgua naiNgua) {
        ChuNgua chuNgua = ngua != null ? chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null) : null;
        LocalDateTime raceDateTime = (schedule != null && schedule.getNgayThiDau() != null)
                ? LocalDateTime.of(schedule.getNgayThiDau(), schedule.getGioBatDau() != null ? schedule.getGioBatDau() : LocalTime.MIDNIGHT)
                : null;

        return RegistrationResponseDTO.builder()
                .id(registration.getMaDangKy())
                .raceId(registration.getMaChangDua())
                .raceName(schedule != null ? schedule.getTenChangDua() : null)
                .raceDate(raceDateTime)
                .horseId(registration.getMaNgua())
                .horseName(ngua != null ? ngua.getTenNgua() : null)
                .horseCode(registration.getMaNgua())
                .jockeyId(registration.getMaNaiNgua())
                .jockeyName(naiNgua != null ? naiNgua.getHoTen() : null)
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .laneNumber(registration.getSoLan())
                .status(StatusMapper.toRegistrationStatus(registration.getTrangThai()))
                .rejectReason(registration.getLyDoTuChoi())
                .registeredAt(registration.getNgayDangKy())
                .build();
    }

    private LaneResponseDTO mapToLaneResponseDTO(DangKyThiDau registration) {
        Ngua ngua = nguaRepository.findById(registration.getMaNgua()).orElse(null);
        NaiNgua naiNgua = naiNguaRepository.findById(registration.getMaNaiNgua()).orElse(null);
        return LaneResponseDTO.builder()
                .id(registration.getMaDangKy())
                .raceId(registration.getMaChangDua())
                .registrationId(registration.getMaDangKy())
                .horseName(ngua != null ? ngua.getTenNgua() : null)
                .jockeyName(naiNgua != null ? naiNgua.getHoTen() : null)
                .laneNumber(registration.getSoLan())
                .assignedAt(registration.getNgayGanLan())
                .build();
    }

    private String toTrangThai(RegistrationStatus status) {
        return switch (status) {
            case PENDING -> DangKyThiDau.TRANG_THAI_CHO_DUYET;
            case APPROVED -> DangKyThiDau.TRANG_THAI_DA_DUYET;
            case REJECTED -> DangKyThiDau.TRANG_THAI_TU_CHOI;
        };
    }

    private String generateMaDangKy() {
        return "DK" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
