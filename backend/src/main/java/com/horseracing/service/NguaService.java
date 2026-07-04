package com.horseracing.service;

import com.horseracing.dto.common.HorseStatus;
import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.ngua.NguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.Result;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * NguaService - quản lý ngựa đua (thêm, sửa, xóa, duyệt/từ chối/loại, tìm kiếm có phân trang).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NguaService {

    private final NguaRepository nguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;
    private final UpdateRequestService updateRequestService;

    public NguaResponseDTO createHorse(NguaRequestDTO dto, String ownerMaTK, String staffId) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));

        String maNgua = (dto.getCode() != null && !dto.getCode().isBlank()) ? dto.getCode() : generateMaNgua();
        nguaRepository.findById(maNgua).ifPresent(existing -> {
            HorseStatus existingStatus = StatusMapper.toHorseStatus(existing.getTrangThai());
            if (existingStatus != HorseStatus.REJECTED) {
                // Chỉ chặn trùng mã với ngựa đang chờ duyệt/đã duyệt/bị loại; mã đã bị TỪ CHỐI được phép tạo lại.
                throw new DuplicateResourceException("Mã ngựa '" + existing.getMaNgua() + "' đã tồn tại");
            }
            nguaRepository.delete(existing);
        });

        Ngua ngua = Ngua.builder()
                .maNgua(maNgua)
                .maChuNgua(chuNgua.getMaChuNgua())
                .tenNgua(dto.getName())
                .giongNgua(dto.getBreed())
                .ngaySinh(parseDate(dto.getDateOfBirth()))
                .gioiTinh(dto.getGender() != null
                        ? Ngua.GioiTinh.valueOf(StatusMapper.toGioiTinh(com.horseracing.dto.common.Gender.valueOf(dto.getGender())))
                        : null)
                .mauLong(dto.getColor())
                .troiLuong(dto.getWeight())
                .trangThai(StatusMapper.toTrangThaiNgua(HorseStatus.PENDING))
                .build();

        Ngua saved = nguaRepository.save(ngua);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_HORSE", "Horse:" + saved.getMaNgua(),
                "Tạo ngựa mới: " + saved.getTenNgua());

        notificationService.notifyAdmins(
                "Ngựa mới chờ duyệt",
                "Chủ ngựa " + chuNgua.getHoTen() + " đã đăng ký ngựa mới '" + saved.getTenNgua() + "' cần duyệt.",
                NotificationType.SYSTEM, "HORSE", saved.getMaNgua());

        return mapToResponseDTO(saved, chuNgua);
    }

    /**
     * Sửa thông tin ngựa:
     * - PENDING / REJECTED: cập nhật trực tiếp (chưa duyệt, không cần qua UpdateRequest).
     * - APPROVED: tạo YeuCauCapNhat chờ Ban tổ chức duyệt, dữ liệu gốc giữ nguyên cho đến khi duyệt.
     */
    public NguaResponseDTO updateHorse(String maNgua, NguaRequestDTO dto, String staffId, boolean privileged) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", ngua.getMaChuNgua()));

        assertOwnership(chuNgua, staffId, privileged);

        HorseStatus currentStatus = StatusMapper.toHorseStatus(ngua.getTrangThai());
        if (HorseStatus.APPROVED.equals(currentStatus)) {
            updateRequestService.createForHorse(ngua, dto, chuNgua);
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_UPDATE_HORSE", "Horse:" + maNgua,
                    "Gửi yêu cầu cập nhật thông tin ngựa: " + ngua.getTenNgua());
        } else {
            ngua.setTenNgua(dto.getName());
            ngua.setGiongNgua(dto.getBreed());
            ngua.setNgaySinh(parseDate(dto.getDateOfBirth()));
            if (dto.getGender() != null) {
                ngua.setGioiTinh(Ngua.GioiTinh.valueOf(
                        StatusMapper.toGioiTinh(com.horseracing.dto.common.Gender.valueOf(dto.getGender()))));
            }
            ngua.setMauLong(dto.getColor());
            ngua.setTroiLuong(dto.getWeight());
            nguaRepository.save(ngua);
            nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_HORSE", "Horse:" + maNgua,
                    "Cập nhật trực tiếp thông tin ngựa: " + ngua.getTenNgua());
        }

        return mapToResponseDTO(ngua, chuNgua);
    }

    public void deleteHorse(String maNgua, String staffId, boolean privileged) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", ngua.getMaChuNgua()));
        assertOwnership(chuNgua, staffId, privileged);

        if (nguaRepository.countUpcomingRaces(maNgua) > 0) {
            throw new ResourceInUseException(
                    "Không thể xóa ngựa '" + ngua.getTenNgua() + "' vì đang có lịch thi đấu sắp diễn ra.");
        }

        nguaRepository.delete(ngua);
        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_HORSE", "Horse:" + maNgua,
                "Đã xóa ngựa: " + ngua.getTenNgua());
    }

    public NguaResponseDTO approveHorse(String maNgua, String staffId) {
        return changeStatus(maNgua, HorseStatus.APPROVED, "APPROVE_HORSE", null, staffId);
    }

    public NguaResponseDTO rejectHorse(String maNgua, String reason, String staffId) {
        return changeStatus(maNgua, HorseStatus.REJECTED, "REJECT_HORSE", reason, staffId);
    }

    public NguaResponseDTO disqualifyHorse(String maNgua, String reason, String staffId) {
        return changeStatus(maNgua, HorseStatus.DISQUALIFIED, "DISQUALIFY_HORSE", reason, staffId);
    }

    private NguaResponseDTO changeStatus(String maNgua, HorseStatus status, String action, String reason, String staffId) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));

        ngua.setTrangThai(StatusMapper.toTrangThaiNgua(status));
        Ngua updated = nguaRepository.save(ngua);

        String desc = "Cập nhật trạng thái ngựa " + updated.getTenNgua() + " -> " + status
                + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : "");
        nhatKyHoatDongService.writeAuditLog(staffId, action, "Horse:" + maNgua, desc);

        ChuNgua chuNgua = chuNguaRepository.findById(updated.getMaChuNgua()).orElse(null);
        if (chuNgua != null) {
            notificationService.notify(chuNgua.getMaTK(),
                    "Cập nhật trạng thái ngựa",
                    "Ngựa " + updated.getTenNgua() + " đã chuyển trạng thái -> " + status
                            + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : ""),
                    status == HorseStatus.APPROVED ? NotificationType.APPROVAL : NotificationType.REJECTION,
                    "HORSE", maNgua);
        }
        return mapToResponseDTO(updated, chuNgua);
    }

    @Transactional(readOnly = true)
    public NguaResponseDTO getHorseById(String maNgua) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null);
        return mapToResponseDTO(ngua, chuNgua);
    }

    @Transactional(readOnly = true)
    public PageResponse<NguaResponseDTO> getAllHorses(Pageable pageable, String keyword, HorseStatus status, String ownerId) {
        Specification<Ngua> spec = buildSpecification(keyword, status, ownerId);
        return PageResponse.of(nguaRepository.findAll(spec, pageable), ngua ->
                mapToResponseDTO(ngua, chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public PageResponse<NguaResponseDTO> getHorsesByOwner(String ownerMaTK, Pageable pageable) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        return PageResponse.of(nguaRepository.findByMaChuNgua(chuNgua.getMaChuNgua(), pageable),
                ngua -> mapToResponseDTO(ngua, chuNgua));
    }

    @Transactional(readOnly = true)
    public List<com.horseracing.dto.result.RaceHistoryItemDTO> getRaceHistory(String maNgua) {
        nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));

        return resultRepository.findByMaNgua(maNgua).stream()
                .map(this::mapResultToHistoryDTO)
                .collect(Collectors.toList());
    }

    private com.horseracing.dto.result.RaceHistoryItemDTO mapResultToHistoryDTO(Result result) {
        String tenChangDua = scheduleRepository.findById(result.getMaChangDua())
                .map(com.horseracing.entity.Schedule::getTenChangDua).orElse("N/A");
        return com.horseracing.dto.result.RaceHistoryItemDTO.builder()
                .raceId(result.getMaChangDua())
                .raceName(tenChangDua)
                .finishPosition(result.getHang())
                .finishTime(result.getThoiGianHoanThanh())
                .pointsEarned(result.getDiem())
                .published(Result.TRANG_THAI_DA_CONG_BO.equals(result.getTrangThaiCongBo()))
                .publishedAt(result.getNgayCongBo())
                .build();
    }

    private Specification<Ngua> buildSpecification(String keyword, HorseStatus status, String ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tenNgua")), "%" + keyword.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), StatusMapper.toTrangThaiNgua(status)));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                String maChuNgua = chuNguaRepository.findByMaTK(ownerId).map(ChuNgua::getMaChuNgua).orElse(ownerId);
                predicates.add(cb.equal(root.get("maChuNgua"), maChuNgua));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NguaResponseDTO mapToResponseDTO(Ngua ngua, ChuNgua chuNgua) {
        return NguaResponseDTO.builder()
                .id(ngua.getMaNgua())
                .code(ngua.getMaNgua())
                .name(ngua.getTenNgua())
                .breed(ngua.getGiongNgua())
                .dateOfBirth(ngua.getNgaySinh())
                .gender(ngua.getGioiTinh() != null ? StatusMapper.toGender(ngua.getGioiTinh().name()) : null)
                .color(ngua.getMauLong())
                .weight(ngua.getTroiLuong())
                .status(StatusMapper.toHorseStatus(ngua.getTrangThai()))
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .createdAt(ngua.getNgayTao())
                .build();
    }

    /** Chặn chủ ngựa A sửa/xóa ngựa của chủ ngựa B (ADMIN/ORGANIZER được bỏ qua). */
    private void assertOwnership(ChuNgua chuNgua, String requesterMaTK, boolean privileged) {
        if (!privileged && !chuNgua.getMaTK().equals(requesterMaTK)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền thao tác trên ngựa của chủ sở hữu khác");
        }
    }

    private String generateMaNgua() {
        return "N" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
