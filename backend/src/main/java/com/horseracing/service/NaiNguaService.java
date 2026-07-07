package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.nainghua.NaiNguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.NaiNgua;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.NaiNguaRepository;
import com.horseracing.repository.TepTinRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * NaiNguaService - quản lý hồ sơ nài ngựa (Jockey) của chủ ngựa: thêm, sửa, xóa, tìm kiếm có phân trang.
 * Hồ sơ không còn trạng thái duyệt riêng - việc duyệt chỉ diễn ra ở cấp đăng ký thi đấu
 * (xem RegistrationService/DangKyThiDau).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NaiNguaService {

    private static final String TARGET_TYPE_DANG_KY = "DANG_KY";
    private static final String FILE_TYPE_JOCKEY_AVATAR = "JOCKEY_AVATAR";
    private static final String FILE_TYPE_LICENSE_SCAN = "LICENSE_SCAN";
    private static final String FILE_TYPE_MEDICAL_CERT = "MEDICAL_CERTIFICATE";

    private final NaiNguaRepository naiNguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final TepTinRepository tepTinRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
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
                .build();

        NaiNgua saved = naiNguaRepository.save(naiNgua);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_JOCKEY", "Jockey:" + saved.getMaNaiNgua(),
                "Tạo jockey mới: " + saved.getHoTen());

        return mapToResponseDTO(saved, chuNgua);
    }

    /**
     * Sửa thông tin jockey (mục 2.5):
     * - Jockey CHƯA có đăng ký nào ở trạng thái APPROVED -> chủ ngựa sửa tự do, áp dụng ngay.
     * - Jockey ĐANG có đăng ký APPROVED tại 1 hoặc nhiều Ban tổ chức -> gửi YeuCauCapNhat tới TỪNG BTC đó,
     *   dữ liệu gốc giữ nguyên cho tới khi có BTC duyệt (BTC nào duyệt trước thì áp dụng - xem UpdateRequestService).
     */
    public NaiNguaResponseDTO updateJockey(String maNaiNgua, NaiNguaRequestDTO dto, String staffId, boolean privileged) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        ChuNgua chuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", naiNgua.getMaChuNgua()));
        assertOwnership(chuNgua, staffId, privileged);

        if (dto.getLicenseNumber() != null
                && naiNguaRepository.existsBySoGiayPhepAndMaNaiNguaNot(dto.getLicenseNumber(), maNaiNgua)) {
            throw new DuplicateResourceException(
                    "Số giấy phép '" + dto.getLicenseNumber() + "' đã được sử dụng bởi jockey khác.");
        }

        List<String> activeOrganizerIds = dangKyThiDauRepository.findDistinctOrganizerIdsByApprovedJockey(maNaiNgua);
        if (!activeOrganizerIds.isEmpty()) {
            for (String organizerId : activeOrganizerIds) {
                updateRequestService.createForJockey(naiNgua, dto, chuNgua, organizerId);
            }
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_UPDATE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Gửi yêu cầu cập nhật thông tin jockey " + naiNgua.getHoTen() + " tới " + activeOrganizerIds.size()
                            + " Ban tổ chức đang có đăng ký đã duyệt");
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

    /**
     * Lỗi 6: hồ sơ chưa từng có đăng ký thi đấu nào -> xóa cứng như trước. Đã từng có đăng ký (bất kể
     * trạng thái đăng ký đó là gì) -> xóa mềm (chuyển "Ngừng hoạt động") để giữ nguyên lịch sử thi đấu/
     * kết quả/bảng xếp hạng và tránh lỗi khóa ngoại khi hiển thị thẳng ra người dùng.
     */
    public void deleteJockey(String maNaiNgua, String staffId, boolean privileged) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", naiNgua.getMaChuNgua()));
        assertOwnership(chuNgua, staffId, privileged);

        if (dangKyThiDauRepository.findByMaNaiNgua(maNaiNgua).isEmpty()) {
            naiNguaRepository.delete(naiNgua);
            nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Đã xóa jockey: " + naiNgua.getHoTen());
        } else {
            naiNgua.setTrangThai(NaiNgua.TRANG_THAI_NGUNG_HOAT_DONG);
            naiNguaRepository.save(naiNgua);
            nhatKyHoatDongService.writeAuditLog(staffId, "DEACTIVATE_JOCKEY", "Jockey:" + maNaiNgua,
                    "Đã chuyển jockey '" + naiNgua.getHoTen() + "' sang Ngừng hoạt động (đã từng có đăng ký thi đấu, không xóa cứng được)");
        }
    }

    /**
     * privileged = ADMIN/ORGANIZER. Quan trọng: publicView không được suy chỉ từ role "có phải
     * HORSE_OWNER hay không" - phải kiểm tra requester có ĐÚNG LÀ chủ của con nài này không, nếu
     * không thì 1 chủ ngựa A có thể xem lộ licenseNumber/licenseScanUrl của chủ ngựa B qua GET /jockeys/{id}.
     */
    @Transactional(readOnly = true)
    public NaiNguaResponseDTO getJockeyById(String maNaiNgua, String requesterMaTK, boolean privileged) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua()).orElse(null);
        boolean isOwner = chuNgua != null && chuNgua.getMaTK().equals(requesterMaTK);
        boolean publicView = !privileged && !isOwner;
        // Mục 4.2: khán giả (không phải chủ sở hữu, không phải ADMIN/ORGANIZER) chỉ xem được nài đã
        // có ít nhất 1 đăng ký APPROVED ở đâu đó.
        if (publicView && !dangKyThiDauRepository.findDistinctApprovedJockeyIds().contains(maNaiNgua)) {
            throw new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua);
        }
        return mapToResponseDTO(naiNgua, chuNgua, publicView);
    }

    @Transactional(readOnly = true)
    public PageResponse<NaiNguaResponseDTO> getAllJockeys(Pageable pageable, String keyword, String ownerId, boolean publicOnly, boolean includeInactive) {
        Specification<NaiNgua> spec = buildSpecification(keyword, ownerId, publicOnly, includeInactive);
        return PageResponse.of(naiNguaRepository.findAll(spec, pageable), j ->
                mapToResponseDTO(j, chuNguaRepository.findById(j.getMaChuNgua()).orElse(null), publicOnly));
    }

    @Transactional(readOnly = true)
    public PageResponse<NaiNguaResponseDTO> getJockeysByOwner(String ownerMaTK, Pageable pageable) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        return PageResponse.of(naiNguaRepository.findByMaChuNgua(chuNgua.getMaChuNgua(), pageable),
                j -> mapToResponseDTO(j, chuNgua));
    }

    private Specification<NaiNgua> buildSpecification(String keyword, String ownerId, boolean publicOnly, boolean includeInactive) {
        List<String> approvedJockeyIds = publicOnly ? dangKyThiDauRepository.findDistinctApprovedJockeyIds() : null;
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("hoTen")), "%" + keyword.toLowerCase() + "%"));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                String maChuNgua = chuNguaRepository.findByMaTK(ownerId).map(ChuNgua::getMaChuNgua).orElse(ownerId);
                predicates.add(cb.equal(root.get("maChuNgua"), maChuNgua));
            }
            if (approvedJockeyIds != null) {
                // Mục 4.2: khán giả chỉ thấy nài đã có ít nhất 1 đăng ký thi đấu được duyệt (không lọc
                // theo trạng thái Hoạt động/Ngừng hoạt động - giữ nguyên lịch sử cho khán giả - lỗi 6).
                predicates.add(approvedJockeyIds.isEmpty() ? cb.disjunction() : root.get("maNaiNgua").in(approvedJockeyIds));
            } else if (!includeInactive) {
                // Danh sách "Nài của tôi" mặc định chỉ hiện hồ sơ đang Hoạt động - lỗi 6.
                predicates.add(cb.or(cb.equal(root.get("trangThai"), NaiNgua.TRANG_THAI_HOAT_DONG), cb.isNull(root.get("trangThai"))));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NaiNguaResponseDTO mapToResponseDTO(NaiNgua naiNgua, ChuNgua chuNgua) {
        return mapToResponseDTO(naiNgua, chuNgua, false);
    }

    /**
     * publicView = true (khán giả): ẩn số giấy phép và tài liệu nhạy cảm, khớp mục 6.2. avatarUrl luôn
     * là endpoint công khai /jockeys/{id}/avatar; licenseScanUrl/medicalCertUrl trỏ tới /upload/{id}
     * (cần đăng nhập) nên chỉ trả cho chủ ngựa/Ban tổ chức/Admin.
     */
    private NaiNguaResponseDTO mapToResponseDTO(NaiNgua naiNgua, ChuNgua chuNgua, boolean publicView) {
        boolean hasAvatar = getJockeyAvatarFile(naiNgua.getMaNaiNgua()).isPresent();
        return NaiNguaResponseDTO.builder()
                .id(naiNgua.getMaNaiNgua())
                .fullName(naiNgua.getHoTen())
                .dateOfBirth(naiNgua.getNgaySinh())
                .gender(naiNgua.getGioiTinh() != null ? StatusMapper.toGender(naiNgua.getGioiTinh().name()) : null)
                .experienceYears(naiNgua.getKinhNghiem())
                .weight(naiNgua.getCanNang())
                .licenseNumber(publicView ? null : naiNgua.getSoGiayPhep())
                .avatarUrl(hasAvatar ? "/jockeys/" + naiNgua.getMaNaiNgua() + "/avatar" : null)
                .licenseScanUrl(publicView ? null : resolveLatestFile(naiNgua.getMaNaiNgua(), FILE_TYPE_LICENSE_SCAN)
                        .map(t -> "/upload/" + t.getMaTepTin()).orElse(null))
                .medicalCertUrl(publicView ? null : resolveLatestFile(naiNgua.getMaNaiNgua(), FILE_TYPE_MEDICAL_CERT)
                        .map(t -> "/upload/" + t.getMaTepTin()).orElse(null))
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .active(!NaiNgua.TRANG_THAI_NGUNG_HOAT_DONG.equals(naiNgua.getTrangThai()))
                .createdAt(naiNgua.getNgayTao())
                .build();
    }

    /** File ảnh đại diện mới nhất của nài (trong số các lần đăng ký, mọi trạng thái) - dùng cho endpoint /jockeys/{id}/avatar. */
    @Transactional(readOnly = true)
    public Optional<TepTin> getJockeyAvatarFile(String maNaiNgua) {
        return resolveLatestFile(maNaiNgua, FILE_TYPE_JOCKEY_AVATAR);
    }

    private Optional<TepTin> resolveLatestFile(String maNaiNgua, String loaiFile) {
        List<String> registrationIds = dangKyThiDauRepository.findByMaNaiNgua(maNaiNgua).stream()
                .map(DangKyThiDau::getMaDangKy).collect(Collectors.toList());
        if (registrationIds.isEmpty()) {
            return Optional.empty();
        }
        return tepTinRepository.findByLoaiDoiTuongAndMaDoiTuongInOrderByNgayTaoDesc(TARGET_TYPE_DANG_KY, registrationIds).stream()
                .filter(t -> loaiFile.equals(t.getLoaiFile()))
                .findFirst();
    }

    /** Chặn chủ ngựa A sửa/xóa jockey của chủ ngựa B (ADMIN/ORGANIZER được bỏ qua). */
    private void assertOwnership(ChuNgua chuNgua, String requesterMaTK, boolean privileged) {
        if (!privileged && !chuNgua.getMaTK().equals(requesterMaTK)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền thao tác trên jockey của chủ sở hữu khác");
        }
    }

    private String generateMaNaiNgua() {
        return "NN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
