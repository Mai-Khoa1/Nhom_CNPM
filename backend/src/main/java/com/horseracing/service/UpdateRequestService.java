package com.horseracing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseracing.dto.common.*;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.upload.TepTinRequestDTO;
import com.horseracing.dto.updaterequest.UpdateRequestResponseDTO;
import com.horseracing.entity.BanToChuc;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.NaiNgua;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.TepTin;
import com.horseracing.entity.YeuCauCapNhat;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.BanToChucRepository;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.TepTinRepository;
import com.horseracing.repository.YeuCauCapNhatRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UpdateRequestService - quản lý yêu cầu chủ ngựa sửa thông tin Ngựa/Nài đã được duyệt trước đó.
 * Dữ liệu gốc trong Ngua/Jockey chỉ bị ghi đè khi Ban tổ chức DUYỆT yêu cầu; nếu TỪ CHỐI thì dữ
 * liệu gốc giữ nguyên, chủ ngựa nhận thông báo kèm lý do.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateRequestService {

    private final YeuCauCapNhatRepository updateRequestRepository;
    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;
    private final TepTinRepository tepTinRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final BanToChucRepository banToChucRepository;
    private final NotificationService notificationService;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final ObjectMapper objectMapper;

    /**
     * Gửi yêu cầu cập nhật thông tin ngựa tới 1 Ban tổ chức cụ thể (organizerId = maBTC). Ngựa đang
     * APPROVED ở nhiều BTC cùng lúc thì NguaService gọi phương thức này 1 lần cho mỗi BTC liên quan -
     * BTC nào duyệt trước, dữ liệu được áp dụng ngay và các yêu cầu song song còn lại tự đóng (xem approve()).
     */
    public void createForHorse(Ngua current, NguaRequestDTO newData, ChuNgua owner, String organizerId) {
        NguaRequestDTO oldData = NguaRequestDTO.builder()
                .code(current.getMaNgua())
                .name(current.getTenNgua())
                .breed(current.getGiongNgua())
                .dateOfBirth(current.getNgaySinh() != null ? current.getNgaySinh().toString() : null)
                .gender(current.getGioiTinh() != null
                        ? StatusMapper.toGender(current.getGioiTinh().name()).name() : null)
                .color(current.getMauLong())
                .weight(current.getTroiLuong())
                .build();

        YeuCauCapNhat request = YeuCauCapNhat.builder()
                .maYeuCau(generateMaYeuCau())
                .loaiDoiTuong(YeuCauCapNhat.LOAI_NGUA)
                .maDoiTuong(current.getMaNgua())
                .maTK(owner.getMaTK())
                .maBTC(organizerId)
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(newData))
                .build();
        updateRequestRepository.save(request);

        notifyOrganizer(organizerId, "Yêu cầu cập nhật thông tin ngựa",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu cập nhật thông tin ngựa '" + current.getTenNgua() + "', cần duyệt.",
                "HORSE_UPDATE_REQUEST", request.getMaYeuCau());
    }

    /** Tương tự createForHorse nhưng cho nài ngựa. */
    public void createForJockey(NaiNgua current, NaiNguaRequestDTO newData, ChuNgua owner, String organizerId) {
        NaiNguaRequestDTO oldData = NaiNguaRequestDTO.builder()
                .fullName(current.getHoTen())
                .dateOfBirth(current.getNgaySinh() != null ? current.getNgaySinh().toString() : null)
                .gender(current.getGioiTinh() != null
                        ? StatusMapper.toGender(current.getGioiTinh().name()).name() : null)
                .experienceYears(current.getKinhNghiem())
                .weight(current.getCanNang())
                .licenseNumber(current.getSoGiayPhep())
                .build();

        YeuCauCapNhat request = YeuCauCapNhat.builder()
                .maYeuCau(generateMaYeuCau())
                .loaiDoiTuong(YeuCauCapNhat.LOAI_NAI_NGUA)
                .maDoiTuong(current.getMaNaiNgua())
                .maTK(owner.getMaTK())
                .maBTC(organizerId)
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(newData))
                .build();
        updateRequestRepository.save(request);

        notifyOrganizer(organizerId, "Yêu cầu cập nhật thông tin nài ngựa",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu cập nhật thông tin nài '" + current.getHoTen() + "', cần duyệt.",
                "JOCKEY_UPDATE_REQUEST", request.getMaYeuCau());
    }

    private void notifyOrganizer(String organizerId, String title, String message, String targetType, String targetId) {
        BanToChuc banToChuc = banToChucRepository.findById(organizerId).orElse(null);
        if (banToChuc != null) {
            notificationService.notify(banToChuc.getMaTK(), title, message, NotificationType.SYSTEM, targetType, targetId);
        }
    }

    /** Chủ ngựa sửa metadata một tệp tin gắn với đăng ký đã APPROVED - chờ đúng Ban tổ chức đó duyệt lại, tệp gốc giữ nguyên. */
    public void createForFile(TepTin current, TepTinRequestDTO newData, ChuNgua owner, String organizerId) {
        TepTinRequestDTO oldData = TepTinRequestDTO.builder()
                .tenFile(current.getTenFile())
                .loaiFile(current.getLoaiFile())
                .targetType(current.getLoaiDoiTuong())
                .targetId(current.getMaDoiTuong())
                .build();

        YeuCauCapNhat request = YeuCauCapNhat.builder()
                .maYeuCau(generateMaYeuCau())
                .loaiDoiTuong(YeuCauCapNhat.LOAI_TEP_TIN)
                .hanhDong(YeuCauCapNhat.HANH_DONG_CAP_NHAT)
                .maDoiTuong(current.getMaTepTin())
                .maTK(owner.getMaTK())
                .maBTC(organizerId)
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(newData))
                .build();
        updateRequestRepository.save(request);

        notifyOrganizer(organizerId, "Yêu cầu cập nhật tệp tin",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu cập nhật tệp tin '" + current.getTenFile() + "', cần duyệt.",
                "FILE_UPDATE_REQUEST", request.getMaYeuCau());
    }

    /** Chủ ngựa xóa một tệp tin gắn với đăng ký đã APPROVED - chờ đúng Ban tổ chức đó duyệt trước khi xóa thật. */
    public void createDeleteRequestForFile(TepTin current, ChuNgua owner, String organizerId) {
        TepTinRequestDTO oldData = TepTinRequestDTO.builder()
                .tenFile(current.getTenFile())
                .loaiFile(current.getLoaiFile())
                .targetType(current.getLoaiDoiTuong())
                .targetId(current.getMaDoiTuong())
                .build();

        YeuCauCapNhat request = YeuCauCapNhat.builder()
                .maYeuCau(generateMaYeuCau())
                .loaiDoiTuong(YeuCauCapNhat.LOAI_TEP_TIN)
                .hanhDong(YeuCauCapNhat.HANH_DONG_XOA)
                .maDoiTuong(current.getMaTepTin())
                .maTK(owner.getMaTK())
                .maBTC(organizerId)
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(oldData))
                .build();
        updateRequestRepository.save(request);

        notifyOrganizer(organizerId, "Yêu cầu xóa tệp tin",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu xóa tệp tin '" + current.getTenFile() + "', cần duyệt.",
                "FILE_DELETE_REQUEST", request.getMaYeuCau());
    }

    public UpdateRequestResponseDTO approve(String maYeuCau, String staffId, boolean isAdmin, String organizerScopeId) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        assertNotAdminForFile(request, isAdmin);
        assertOrganizerScope(request, organizerScopeId);
        requirePending(request);

        if (YeuCauCapNhat.LOAI_NGUA.equals(request.getLoaiDoiTuong())) {
            applyHorseUpdate(request);
        } else if (YeuCauCapNhat.LOAI_NAI_NGUA.equals(request.getLoaiDoiTuong())) {
            applyJockeyUpdate(request);
        } else {
            applyFileChange(request);
        }

        request.setTrangThai(YeuCauCapNhat.TRANG_THAI_DA_DUYET);
        request.setNgayXuLy(LocalDateTime.now());
        YeuCauCapNhat updated = updateRequestRepository.save(request);

        nhatKyHoatDongService.writeAuditLog(staffId, "APPROVE_UPDATE_REQUEST", "UpdateRequest:" + maYeuCau,
                "Đã duyệt yêu cầu " + describeAction(request) + " " + request.getLoaiDoiTuong() + ":" + request.getMaDoiTuong());

        boolean isDelete = YeuCauCapNhat.HANH_DONG_XOA.equals(request.getHanhDong());
        notificationService.notify(updated.getMaTK(),
                isDelete ? "Yêu cầu xóa đã được duyệt" : "Yêu cầu cập nhật đã được duyệt",
                isDelete
                        ? "Yêu cầu xóa tệp tin của bạn đã được Ban tổ chức duyệt, tệp đã bị xóa."
                        : "Thông tin cập nhật của bạn đã được Ban tổ chức duyệt và áp dụng.",
                NotificationType.APPROVAL, targetTypeLabel(updated), updated.getMaDoiTuong());

        autoCloseSiblingRequests(updated, staffId);

        return mapToResponseDTO(updated);
    }

    /**
     * Nguyên tắc "duyệt đầu tiên thắng" (mục 2.5): khi 1 ngựa/nài đang APPROVED ở nhiều Ban tổ chức,
     * chủ ngựa sửa thông tin sẽ tạo nhiều YeuCauCapNhat song song (mỗi BTC 1 yêu cầu, cùng dữ liệu mới).
     * BTC nào duyệt trước, dữ liệu đã được áp dụng ngay ở trên - các yêu cầu song song còn lại (gửi tới
     * BTC khác, cùng đối tượng + cùng dữ liệu mới, còn PENDING) không cần duyệt lại nữa nên tự đóng.
     */
    private void autoCloseSiblingRequests(YeuCauCapNhat approved, String staffId) {
        if (YeuCauCapNhat.LOAI_TEP_TIN.equals(approved.getLoaiDoiTuong())) {
            return;
        }
        List<YeuCauCapNhat> siblings = updateRequestRepository
                .findByLoaiDoiTuongAndMaDoiTuongAndDuLieuMoiAndTrangThaiAndMaYeuCauNot(
                        approved.getLoaiDoiTuong(), approved.getMaDoiTuong(), approved.getDuLieuMoi(),
                        YeuCauCapNhat.TRANG_THAI_CHO_DUYET, approved.getMaYeuCau());
        for (YeuCauCapNhat sibling : siblings) {
            sibling.setTrangThai(YeuCauCapNhat.TRANG_THAI_DA_DUYET);
            sibling.setNgayXuLy(LocalDateTime.now());
            updateRequestRepository.save(sibling);
            nhatKyHoatDongService.writeAuditLog(staffId, "AUTO_CLOSE_UPDATE_REQUEST", "UpdateRequest:" + sibling.getMaYeuCau(),
                    "Tự đóng yêu cầu cập nhật " + sibling.getLoaiDoiTuong() + ":" + sibling.getMaDoiTuong()
                            + " vì đã được duyệt bởi Ban tổ chức khác (" + approved.getMaYeuCau() + ")");
        }
    }

    public UpdateRequestResponseDTO reject(String maYeuCau, String reason, String staffId, boolean isAdmin, String organizerScopeId) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        assertNotAdminForFile(request, isAdmin);
        assertOrganizerScope(request, organizerScopeId);
        requirePending(request);

        request.setTrangThai(YeuCauCapNhat.TRANG_THAI_TU_CHOI);
        request.setLyDoTuChoi(reason);
        request.setNgayXuLy(LocalDateTime.now());
        YeuCauCapNhat updated = updateRequestRepository.save(request);

        nhatKyHoatDongService.writeAuditLog(staffId, "REJECT_UPDATE_REQUEST", "UpdateRequest:" + maYeuCau,
                "Đã từ chối yêu cầu " + describeAction(request) + " " + request.getLoaiDoiTuong() + ":" + request.getMaDoiTuong()
                        + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : ""));

        boolean isDelete = YeuCauCapNhat.HANH_DONG_XOA.equals(request.getHanhDong());
        notificationService.notify(updated.getMaTK(),
                isDelete ? "Yêu cầu xóa bị từ chối" : "Yêu cầu cập nhật bị từ chối",
                (isDelete ? "Yêu cầu xóa tệp tin của bạn đã bị Ban tổ chức từ chối" : "Thông tin cập nhật của bạn đã bị Ban tổ chức từ chối")
                        + (reason != null && !reason.isBlank() ? ": " + reason : ".")
                        + (isDelete ? "" : " Vui lòng sửa lại và gửi lại."),
                NotificationType.REJECTION, targetTypeLabel(updated), updated.getMaDoiTuong());

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public UpdateRequestResponseDTO getById(String maYeuCau, boolean isAdmin, String organizerScopeId) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        assertNotAdminForFile(request, isAdmin);
        assertOrganizerScope(request, organizerScopeId);
        return mapToResponseDTO(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<UpdateRequestResponseDTO> getAll(Pageable pageable, UpdateRequestStatus status, UpdateTargetType targetType,
            boolean isAdmin, String organizerScopeId) {
        if (isAdmin && targetType == UpdateTargetType.FILE) {
            throw new AccessDeniedException("Admin không có quyền xem yêu cầu tệp tin");
        }
        Specification<YeuCauCapNhat> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), toTrangThai(status)));
            }
            if (targetType != null) {
                predicates.add(cb.equal(root.get("loaiDoiTuong"), toLoaiDoiTuong(targetType)));
            }
            if (isAdmin) {
                // Admin không được thao tác/xem yêu cầu liên quan tệp tin, kể cả khi không lọc theo loại.
                predicates.add(cb.notEqual(root.get("loaiDoiTuong"), YeuCauCapNhat.LOAI_TEP_TIN));
            }
            if (organizerScopeId != null) {
                // Ban tổ chức chỉ thấy yêu cầu gửi tới đúng mình (multi-tenancy).
                predicates.add(cb.equal(root.get("maBTC"), organizerScopeId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(updateRequestRepository.findAll(spec, pageable), this::mapToResponseDTO);
    }

    // ----- Helper -----

    /** Chặn Ban tổ chức A duyệt/xem yêu cầu gửi tới Ban tổ chức B (multi-tenancy). ADMIN (organizerScopeId == null) xem được tất cả. */
    private void assertOrganizerScope(YeuCauCapNhat request, String organizerScopeId) {
        if (organizerScopeId != null && request.getMaBTC() != null && !organizerScopeId.equals(request.getMaBTC())) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên yêu cầu cập nhật của Ban tổ chức khác");
        }
    }

    /** Admin không được thao tác/xem yêu cầu liên quan tệp tin (chỉ Ban tổ chức mới được duyệt tệp tin). */
    private void assertNotAdminForFile(YeuCauCapNhat request, boolean isAdmin) {
        if (isAdmin && YeuCauCapNhat.LOAI_TEP_TIN.equals(request.getLoaiDoiTuong())) {
            throw new AccessDeniedException("Admin không có quyền thao tác trên yêu cầu tệp tin");
        }
    }

    private String describeAction(YeuCauCapNhat request) {
        return YeuCauCapNhat.HANH_DONG_XOA.equals(request.getHanhDong()) ? "xóa" : "cập nhật";
    }

    private void requirePending(YeuCauCapNhat request) {
        if (!YeuCauCapNhat.TRANG_THAI_CHO_DUYET.equals(request.getTrangThai())) {
            throw new ResourceInUseException("Yêu cầu này đã được xử lý trước đó.");
        }
    }

    private void applyHorseUpdate(YeuCauCapNhat request) {
        Ngua ngua = nguaRepository.findById(request.getMaDoiTuong())
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", request.getMaDoiTuong()));
        NguaRequestDTO dto = fromJson(request.getDuLieuMoi(), NguaRequestDTO.class);

        ngua.setTenNgua(dto.getName());
        ngua.setGiongNgua(dto.getBreed());
        ngua.setNgaySinh(parseDate(dto.getDateOfBirth()));
        if (dto.getGender() != null) {
            ngua.setGioiTinh(Ngua.GioiTinh.valueOf(
                    StatusMapper.toGioiTinh(Gender.valueOf(dto.getGender()))));
        }
        ngua.setMauLong(dto.getColor());
        ngua.setTroiLuong(dto.getWeight());
        nguaRepository.save(ngua);
    }

    private void applyJockeyUpdate(YeuCauCapNhat request) {
        NaiNgua naiNgua = naiNguaRepository.findById(request.getMaDoiTuong())
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", request.getMaDoiTuong()));
        NaiNguaRequestDTO dto = fromJson(request.getDuLieuMoi(), NaiNguaRequestDTO.class);

        naiNgua.setHoTen(dto.getFullName());
        naiNgua.setNgaySinh(parseDate(dto.getDateOfBirth()));
        if (dto.getGender() != null) {
            naiNgua.setGioiTinh(Ngua.GioiTinh.valueOf(
                    StatusMapper.toGioiTinh(Gender.valueOf(dto.getGender()))));
        }
        naiNgua.setKinhNghiem(dto.getExperienceYears());
        naiNgua.setCanNang(dto.getWeight());
        naiNgua.setSoGiayPhep(dto.getLicenseNumber());
        naiNguaRepository.save(naiNgua);
    }

    /** Áp dụng thay đổi tệp tin khi yêu cầu được DUYỆT: CAP_NHAT ghi đè metadata, XOA xóa file thật. */
    private void applyFileChange(YeuCauCapNhat request) {
        TepTin tepTin = tepTinRepository.findById(request.getMaDoiTuong())
                .orElseThrow(() -> new ResourceNotFoundException("Tệp tin", "maTepTin", request.getMaDoiTuong()));

        if (YeuCauCapNhat.HANH_DONG_XOA.equals(request.getHanhDong())) {
            try {
                Files.deleteIfExists(Paths.get(tepTin.getDuongDan()));
            } catch (IOException e) {
                throw new UncheckedIOException("Không thể xóa file: " + e.getMessage(), e);
            }
            tepTinRepository.delete(tepTin);
        } else {
            TepTinRequestDTO dto = fromJson(request.getDuLieuMoi(), TepTinRequestDTO.class);
            tepTin.setTenFile(dto.getTenFile());
            tepTin.setLoaiFile(dto.getLoaiFile());
            tepTin.setLoaiDoiTuong(dto.getTargetType());
            tepTin.setMaDoiTuong(dto.getTargetId());
            tepTinRepository.save(tepTin);
        }
    }

    private UpdateRequestResponseDTO mapToResponseDTO(YeuCauCapNhat request) {
        UpdateTargetType targetType = toUpdateTargetType(request.getLoaiDoiTuong());
        Map<String, Object> oldData = fromJsonToMap(request.getDuLieuCu());
        Map<String, Object> newData = fromJsonToMap(request.getDuLieuMoi());
        String nameKey = switch (targetType) {
            case HORSE -> "name";
            case JOCKEY -> "fullName";
            case FILE -> "tenFile";
        };
        String targetName = (String) oldData.getOrDefault(nameKey, request.getMaDoiTuong());
        ChuNgua owner = chuNguaRepository.findByMaTK(request.getMaTK()).orElse(null);

        return UpdateRequestResponseDTO.builder()
                .id(request.getMaYeuCau())
                .targetType(targetType)
                .targetId(request.getMaDoiTuong())
                .targetName(targetName)
                .ownerId(request.getMaTK())
                .ownerName(owner != null ? owner.getHoTen() : null)
                .oldData(oldData)
                .newData(newData)
                .action(request.getHanhDong())
                .status(StatusMapper.toUpdateRequestStatus(request.getTrangThai()))
                .rejectReason(request.getLyDoTuChoi())
                .createdAt(request.getNgayTao())
                .processedAt(request.getNgayXuLy())
                .build();
    }

    private UpdateTargetType toUpdateTargetType(String loaiDoiTuong) {
        if (YeuCauCapNhat.LOAI_NGUA.equals(loaiDoiTuong)) return UpdateTargetType.HORSE;
        if (YeuCauCapNhat.LOAI_NAI_NGUA.equals(loaiDoiTuong)) return UpdateTargetType.JOCKEY;
        return UpdateTargetType.FILE;
    }

    private String targetTypeLabel(YeuCauCapNhat request) {
        return switch (toUpdateTargetType(request.getLoaiDoiTuong())) {
            case HORSE -> "HORSE";
            case JOCKEY -> "JOCKEY";
            case FILE -> "FILE";
        };
    }

    private String toTrangThai(UpdateRequestStatus status) {
        return switch (status) {
            case PENDING -> YeuCauCapNhat.TRANG_THAI_CHO_DUYET;
            case APPROVED -> YeuCauCapNhat.TRANG_THAI_DA_DUYET;
            case REJECTED -> YeuCauCapNhat.TRANG_THAI_TU_CHOI;
        };
    }

    private String toLoaiDoiTuong(UpdateTargetType targetType) {
        return switch (targetType) {
            case HORSE -> YeuCauCapNhat.LOAI_NGUA;
            case JOCKEY -> YeuCauCapNhat.LOAI_NAI_NGUA;
            case FILE -> YeuCauCapNhat.LOAI_TEP_TIN;
        };
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể chuyển dữ liệu sang JSON", e);
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể đọc dữ liệu JSON", e);
        }
    }

    private Map<String, Object> fromJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không thể đọc dữ liệu JSON", e);
        }
    }

    private String generateMaYeuCau() {
        return "YC" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
