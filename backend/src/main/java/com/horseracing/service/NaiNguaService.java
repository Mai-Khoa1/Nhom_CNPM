package com.horseracing.service;

import com.horseracing.dto.common.JockeyStatus;
import com.horseracing.dto.common.NotificationType;
import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.nainghua.NaiNguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.NaiNgua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
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

/**
 * NaiNguaService - quản lý nài ngựa (Jockey): thêm, sửa, xóa, duyệt/từ chối, tìm kiếm có phân trang.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NaiNguaService {

    private final NaiNguaRepository naiNguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final NotificationService notificationService;
    private final UpdateRequestService updateRequestService;

    public NaiNguaResponseDTO createJockey(NaiNguaRequestDTO dto, String ownerMaTK, String staffId) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));

        if (dto.getLicenseNumber() != null && naiNguaRepository.existsBySoGiayPhep(dto.getLicenseNumber())) {
            throw new DuplicateResourceException(
                    "Số giấy phép '" + dto.getLicenseNumber() + "' đã tồn tại trong hệ thống.");
        }

        NaiNgua naiNgua = NaiNgua.builder()
                .maNaiNgua(generateMaNaiNgua())
                .maChuNgua(chuNgua.getMaChuNgua())
                .hoTen(dto.getFullName())
                .ngaySinh(parseDate(dto.getDateOfBirth()))
                .gioiTinh(dto.getGender() != null
                        ? com.horseracing.entity.Ngua.GioiTinh.valueOf(
                                StatusMapper.toGioiTinh(com.horseracing.dto.common.Gender.valueOf(dto.getGender())))
                        : null)
                .kinhNghiem(dto.getExperienceYears())
                .canNang(dto.getWeight())
                .soGiayPhep(dto.getLicenseNumber())
                .trangThai(StatusMapper.toTrangThaiJockey(JockeyStatus.PENDING))
                .build();

        NaiNgua saved = naiNguaRepository.save(naiNgua);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_JOCKEY", "Jockey:" + saved.getMaNaiNgua(),
                "Tạo jockey mới: " + saved.getHoTen());

        notificationService.notifyAdmins(
                "Jockey mới chờ duyệt",
                "Chủ ngựa " + chuNgua.getHoTen() + " đã đăng ký jockey mới '" + saved.getHoTen() + "' cần duyệt.",
                NotificationType.SYSTEM, "JOCKEY", saved.getMaNaiNgua());

        return mapToResponseDTO(saved, chuNgua);
    }

    /**
     * Sửa thông tin jockey:
     * - PENDING / REJECTED: cập nhật trực tiếp (chưa duyệt, không cần qua UpdateRequest).
     * - APPROVED: tạo YeuCauCapNhat chờ Ban tổ chức duyệt, dữ liệu gốc giữ nguyên cho đến khi duyệt.
     */
    public NaiNguaResponseDTO updateJockey(String maNaiNgua, NaiNguaRequestDTO dto, String staffId) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        if (dto.getLicenseNumber() != null
                && naiNguaRepository.existsBySoGiayPhepAndMaNaiNguaNot(dto.getLicenseNumber(), maNaiNgua)) {
            throw new DuplicateResourceException(
                    "Số giấy phép '" + dto.getLicenseNumber() + "' đã được sử dụng bởi jockey khác.");
        }

        ChuNgua chuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", naiNgua.getMaChuNgua()));

        JockeyStatus currentStatus = StatusMapper.toJockeyStatus(naiNgua.getTrangThai());
        if (JockeyStatus.APPROVED.equals(currentStatus) || JockeyStatus.ACTIVE.equals(currentStatus)) {
            updateRequestService.createForJockey(naiNgua, dto, chuNgua);
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_UPDATE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Gửi yêu cầu cập nhật thông tin jockey: " + naiNgua.getHoTen());
        } else {
            naiNgua.setHoTen(dto.getFullName());
            naiNgua.setNgaySinh(parseDate(dto.getDateOfBirth()));
            if (dto.getGender() != null) {
                naiNgua.setGioiTinh(com.horseracing.entity.Ngua.GioiTinh.valueOf(
                        StatusMapper.toGioiTinh(com.horseracing.dto.common.Gender.valueOf(dto.getGender()))));
            }
            naiNgua.setKinhNghiem(dto.getExperienceYears());
            naiNgua.setCanNang(dto.getWeight());
            naiNgua.setSoGiayPhep(dto.getLicenseNumber());
            naiNguaRepository.save(naiNgua);
            nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Cập nhật trực tiếp thông tin jockey: " + naiNgua.getHoTen());
        }

        return mapToResponseDTO(naiNgua, chuNgua);
    }

    public void deleteJockey(String maNaiNgua, String staffId) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        if (naiNguaRepository.countUpcomingRaces(maNaiNgua) > 0) {
            naiNgua.setTrangThai(StatusMapper.toTrangThaiJockey(JockeyStatus.INACTIVE));
            naiNguaRepository.save(naiNgua);

            nhatKyHoatDongService.writeAuditLog(staffId, "DEACTIVATE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Vô hiệu hóa jockey " + naiNgua.getHoTen() + " (đang có lịch thi đấu)");

            throw new ResourceInUseException(
                    "Jockey '" + naiNgua.getHoTen() + "' đang có lịch thi đấu sắp tới. " +
                    "Hệ thống đã chuyển trạng thái thành 'Không hoạt động' thay vì xóa.");
        }

        naiNguaRepository.delete(naiNgua);
        nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_JOCKEY", "Jockey:" + maNaiNgua,
                "Đã xóa jockey: " + naiNgua.getHoTen());
    }

    public NaiNguaResponseDTO approveJockey(String maNaiNgua, String staffId) {
        return changeStatus(maNaiNgua, JockeyStatus.APPROVED, "APPROVE_JOCKEY", null, staffId);
    }

    public NaiNguaResponseDTO rejectJockey(String maNaiNgua, String reason, String staffId) {
        return changeStatus(maNaiNgua, JockeyStatus.REJECTED, "REJECT_JOCKEY", reason, staffId);
    }

    private NaiNguaResponseDTO changeStatus(String maNaiNgua, JockeyStatus status, String action, String reason, String staffId) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        naiNgua.setTrangThai(StatusMapper.toTrangThaiJockey(status));
        NaiNgua updated = naiNguaRepository.save(naiNgua);

        String desc = "Cập nhật trạng thái jockey " + updated.getHoTen() + " -> " + status
                + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : "");
        nhatKyHoatDongService.writeAuditLog(staffId, action, "Jockey:" + maNaiNgua, desc);

        ChuNgua chuNgua = chuNguaRepository.findById(updated.getMaChuNgua()).orElse(null);
        if (chuNgua != null) {
            notificationService.notify(chuNgua.getMaTK(),
                    "Cập nhật trạng thái jockey",
                    "Jockey " + updated.getHoTen() + " đã chuyển trạng thái -> " + status
                            + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : ""),
                    status == JockeyStatus.APPROVED ? NotificationType.APPROVAL : NotificationType.REJECTION,
                    "JOCKEY", maNaiNgua);
        }
        return mapToResponseDTO(updated, chuNgua);
    }

    @Transactional(readOnly = true)
    public NaiNguaResponseDTO getJockeyById(String maNaiNgua) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua()).orElse(null);
        return mapToResponseDTO(naiNgua, chuNgua);
    }

    @Transactional(readOnly = true)
    public PageResponse<NaiNguaResponseDTO> getAllJockeys(Pageable pageable, String keyword, JockeyStatus status, String ownerId) {
        Specification<NaiNgua> spec = buildSpecification(keyword, status, ownerId);
        return PageResponse.of(naiNguaRepository.findAll(spec, pageable), j ->
                mapToResponseDTO(j, chuNguaRepository.findById(j.getMaChuNgua()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public PageResponse<NaiNguaResponseDTO> getJockeysByOwner(String ownerMaTK, Pageable pageable) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        return PageResponse.of(naiNguaRepository.findByMaChuNgua(chuNgua.getMaChuNgua(), pageable),
                j -> mapToResponseDTO(j, chuNgua));
    }

    private Specification<NaiNgua> buildSpecification(String keyword, JockeyStatus status, String ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("hoTen")), "%" + keyword.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), StatusMapper.toTrangThaiJockey(status)));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                String maChuNgua = chuNguaRepository.findByMaTK(ownerId).map(ChuNgua::getMaChuNgua).orElse(ownerId);
                predicates.add(cb.equal(root.get("maChuNgua"), maChuNgua));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NaiNguaResponseDTO mapToResponseDTO(NaiNgua naiNgua, ChuNgua chuNgua) {
        return NaiNguaResponseDTO.builder()
                .id(naiNgua.getMaNaiNgua())
                .fullName(naiNgua.getHoTen())
                .dateOfBirth(naiNgua.getNgaySinh())
                .gender(naiNgua.getGioiTinh() != null ? StatusMapper.toGender(naiNgua.getGioiTinh().name()) : null)
                .experienceYears(naiNgua.getKinhNghiem())
                .weight(naiNgua.getCanNang())
                .licenseNumber(naiNgua.getSoGiayPhep())
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .status(StatusMapper.toJockeyStatus(naiNgua.getTrangThai()))
                .createdAt(naiNgua.getNgayTao())
                .build();
    }

    private String generateMaNaiNgua() {
        return "NN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
