package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.common.StatusMapper;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.ngua.NguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.Result;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.ResultRepository;
import com.horseracing.repository.ScheduleRepository;
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
 * NguaService - quản lý hồ sơ ngựa đua của chủ ngựa (thêm, sửa, xóa, tìm kiếm có phân trang).
 * Hồ sơ không còn trạng thái duyệt riêng - việc duyệt chỉ diễn ra ở cấp đăng ký thi đấu
 * (xem RegistrationService/DangKyThiDau).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NguaService {

    private static final String TARGET_TYPE_DANG_KY = "DANG_KY";
    private static final String FILE_TYPE_HORSE_PHOTO = "HORSE_PHOTO";
    private static final String FILE_TYPE_PASSPORT = "PASSPORT_SCAN";
    private static final String FILE_TYPE_HEALTH_CERT = "HEALTH_CERTIFICATE";

    private final NguaRepository nguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final ResultRepository resultRepository;
    private final ScheduleRepository scheduleRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final TepTinRepository tepTinRepository;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final UpdateRequestService updateRequestService;

    public NguaResponseDTO createHorse(NguaRequestDTO dto, String ownerMaTK, String staffId) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));

        String maNgua = (dto.getCode() != null && !dto.getCode().isBlank()) ? dto.getCode() : generateMaNgua();
        if (nguaRepository.existsById(maNgua)) {
            throw new DuplicateResourceException("Mã ngựa '" + maNgua + "' đã tồn tại");
        }

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
                .build();

        Ngua saved = nguaRepository.save(ngua);
        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_HORSE", "Horse:" + saved.getMaNgua(),
                "Tạo ngựa mới: " + saved.getTenNgua());

        return mapToResponseDTO(saved, chuNgua);
    }

    /**
     * Sửa thông tin ngựa (mục 2.5):
     * - Ngựa CHƯA có đăng ký nào ở trạng thái APPROVED -> chủ ngựa sửa tự do, áp dụng ngay.
     * - Ngựa ĐANG có đăng ký APPROVED tại 1 hoặc nhiều Ban tổ chức -> gửi YeuCauCapNhat tới TỪNG BTC đó,
     *   dữ liệu gốc giữ nguyên cho tới khi có BTC duyệt (BTC nào duyệt trước thì áp dụng - xem UpdateRequestService).
     */
    public NguaResponseDTO updateHorse(String maNgua, NguaRequestDTO dto, String staffId, boolean privileged) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", ngua.getMaChuNgua()));

        assertOwnership(chuNgua, staffId, privileged);

        List<String> activeOrganizerIds = dangKyThiDauRepository.findDistinctOrganizerIdsByApprovedHorse(maNgua);
        if (!activeOrganizerIds.isEmpty()) {
            for (String organizerId : activeOrganizerIds) {
                updateRequestService.createForHorse(ngua, dto, chuNgua, organizerId);
            }
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_UPDATE_HORSE", "Horse:" + maNgua,
                    "Gửi yêu cầu cập nhật thông tin ngựa " + ngua.getTenNgua() + " tới " + activeOrganizerIds.size()
                            + " Ban tổ chức đang có đăng ký đã duyệt");
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

    /**
     * Lỗi 6: hồ sơ chưa từng có đăng ký thi đấu nào -> xóa cứng như trước. Đã từng có đăng ký (bất kể
     * trạng thái đăng ký đó là gì) -> xóa mềm (chuyển "Ngừng hoạt động") để giữ nguyên lịch sử thi đấu/
     * kết quả/bảng xếp hạng và tránh lỗi khóa ngoại khi hiển thị thẳng ra người dùng.
     */
    public void deleteHorse(String maNgua, String staffId, boolean privileged) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", ngua.getMaChuNgua()));
        assertOwnership(chuNgua, staffId, privileged);

        if (dangKyThiDauRepository.findByMaNgua(maNgua).isEmpty()) {
            nguaRepository.delete(ngua);
            nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_HORSE", "Horse:" + maNgua,
                    "Đã xóa ngựa: " + ngua.getTenNgua());
        } else {
            ngua.setTrangThai(Ngua.TRANG_THAI_NGUNG_HOAT_DONG);
            nguaRepository.save(ngua);
            nhatKyHoatDongService.writeAuditLog(staffId, "DEACTIVATE_HORSE", "Horse:" + maNgua,
                    "Đã chuyển ngựa '" + ngua.getTenNgua() + "' sang Ngừng hoạt động (đã từng có đăng ký thi đấu, không xóa cứng được)");
        }
    }

    /**
     * privileged = ADMIN/ORGANIZER. Quan trọng: publicView không được suy chỉ từ role "có phải
     * HORSE_OWNER hay không" - phải kiểm tra requester có ĐÚNG LÀ chủ của con ngựa này không, nếu
     * không thì 1 chủ ngựa A có thể xem lộ passportUrl/healthCertUrl của chủ ngựa B qua GET /horses/{id}.
     */
    @Transactional(readOnly = true)
    public NguaResponseDTO getHorseById(String maNgua, String requesterMaTK, boolean privileged) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        ChuNgua chuNgua = chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null);
        boolean isOwner = chuNgua != null && chuNgua.getMaTK().equals(requesterMaTK);
        boolean publicView = !privileged && !isOwner;
        // Mục 4.2: khán giả (không phải chủ sở hữu, không phải ADMIN/ORGANIZER) chỉ xem được ngựa đã
        // có ít nhất 1 đăng ký APPROVED ở đâu đó.
        if (publicView && !dangKyThiDauRepository.findDistinctApprovedHorseIds().contains(maNgua)) {
            throw new ResourceNotFoundException("Ngựa", "maNgua", maNgua);
        }
        return mapToResponseDTO(ngua, chuNgua, publicView);
    }

    @Transactional(readOnly = true)
    public PageResponse<NguaResponseDTO> getAllHorses(Pageable pageable, String keyword, String ownerId, boolean publicOnly, boolean includeInactive) {
        Specification<Ngua> spec = buildSpecification(keyword, ownerId, publicOnly, includeInactive);
        return PageResponse.of(nguaRepository.findAll(spec, pageable), ngua ->
                mapToResponseDTO(ngua, chuNguaRepository.findById(ngua.getMaChuNgua()).orElse(null), publicOnly));
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

    private Specification<Ngua> buildSpecification(String keyword, String ownerId, boolean publicOnly, boolean includeInactive) {
        List<String> approvedHorseIds = publicOnly ? dangKyThiDauRepository.findDistinctApprovedHorseIds() : null;
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("tenNgua")), "%" + keyword.toLowerCase() + "%"));
            }
            if (ownerId != null && !ownerId.isBlank()) {
                String maChuNgua = chuNguaRepository.findByMaTK(ownerId).map(ChuNgua::getMaChuNgua).orElse(ownerId);
                predicates.add(cb.equal(root.get("maChuNgua"), maChuNgua));
            }
            if (approvedHorseIds != null) {
                // Mục 4.2: khán giả chỉ thấy ngựa đã có ít nhất 1 đăng ký thi đấu được duyệt (không lọc
                // theo trạng thái Hoạt động/Ngừng hoạt động - hồ sơ ngừng hoạt động vẫn hiển thị công khai
                // nếu đã từng được duyệt, giữ nguyên lịch sử thành tích cho khán giả tra cứu - lỗi 6).
                predicates.add(approvedHorseIds.isEmpty() ? cb.disjunction() : root.get("maNgua").in(approvedHorseIds));
            } else if (!includeInactive) {
                // Danh sách "Ngựa của tôi" mặc định chỉ hiện hồ sơ đang Hoạt động - lỗi 6.
                predicates.add(cb.or(cb.equal(root.get("trangThai"), Ngua.TRANG_THAI_HOAT_DONG), cb.isNull(root.get("trangThai"))));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private NguaResponseDTO mapToResponseDTO(Ngua ngua, ChuNgua chuNgua) {
        return mapToResponseDTO(ngua, chuNgua, false);
    }

    /**
     * publicView = true (khán giả/khách chưa đăng nhập): ẩn tài liệu nhạy cảm (hộ chiếu, hồ sơ sức khỏe) -
     * chỉ giữ avatarUrl để hiển thị ảnh đại diện, khớp mục 6.2. avatarUrl luôn là endpoint công khai
     * /horses/{id}/avatar (không cần đăng nhập); passportUrl/healthCertUrl trỏ tới /upload/{id} (cần đăng
     * nhập) nên chỉ trả cho chủ ngựa/Ban tổ chức/Admin.
     */
    private NguaResponseDTO mapToResponseDTO(Ngua ngua, ChuNgua chuNgua, boolean publicView) {
        boolean hasAvatar = getHorseAvatarFile(ngua.getMaNgua()).isPresent();
        return NguaResponseDTO.builder()
                .id(ngua.getMaNgua())
                .code(ngua.getMaNgua())
                .name(ngua.getTenNgua())
                .breed(ngua.getGiongNgua())
                .dateOfBirth(ngua.getNgaySinh())
                .gender(ngua.getGioiTinh() != null ? StatusMapper.toGender(ngua.getGioiTinh().name()) : null)
                .color(ngua.getMauLong())
                .weight(ngua.getTroiLuong())
                .avatarUrl(hasAvatar ? "/horses/" + ngua.getMaNgua() + "/avatar" : null)
                .passportUrl(publicView ? null : resolveLatestFile(ngua.getMaNgua(), FILE_TYPE_PASSPORT, this::registrationIdsOf)
                        .map(t -> "/upload/" + t.getMaTepTin()).orElse(null))
                .healthCertUrl(publicView ? null : resolveLatestFile(ngua.getMaNgua(), FILE_TYPE_HEALTH_CERT, this::registrationIdsOf)
                        .map(t -> "/upload/" + t.getMaTepTin()).orElse(null))
                .ownerId(chuNgua != null ? chuNgua.getMaTK() : null)
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .active(!Ngua.TRANG_THAI_NGUNG_HOAT_DONG.equals(ngua.getTrangThai()))
                .createdAt(ngua.getNgayTao())
                .build();
    }

    /** File ảnh đại diện mới nhất của ngựa (trong số các lần đăng ký, mọi trạng thái) - dùng cho endpoint /horses/{id}/avatar. */
    @Transactional(readOnly = true)
    public Optional<TepTin> getHorseAvatarFile(String maNgua) {
        return resolveLatestFile(maNgua, FILE_TYPE_HORSE_PHOTO, this::registrationIdsOf);
    }

    private List<String> registrationIdsOf(String maNgua) {
        return dangKyThiDauRepository.findByMaNgua(maNgua).stream().map(DangKyThiDau::getMaDangKy).collect(Collectors.toList());
    }

    private Optional<TepTin> resolveLatestFile(String targetId, String loaiFile, java.util.function.Function<String, List<String>> registrationIdsResolver) {
        List<String> registrationIds = registrationIdsResolver.apply(targetId);
        if (registrationIds.isEmpty()) {
            return Optional.empty();
        }
        return tepTinRepository.findByLoaiDoiTuongAndMaDoiTuongInOrderByNgayTaoDesc(TARGET_TYPE_DANG_KY, registrationIds).stream()
                .filter(t -> loaiFile.equals(t.getLoaiFile()))
                .findFirst();
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
