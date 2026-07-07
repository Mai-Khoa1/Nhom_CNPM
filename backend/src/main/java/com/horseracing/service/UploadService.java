package com.horseracing.service;

import com.horseracing.dto.common.PageResponse;
import com.horseracing.dto.upload.FileUploadResponseDTO;
import com.horseracing.dto.upload.TepTinRequestDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.DangKyThiDau;
import com.horseracing.entity.MuaGiai;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.Schedule;
import com.horseracing.entity.TepTin;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.DangKyThiDauRepository;
import com.horseracing.repository.MuaGiaiRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.ScheduleRepository;
import com.horseracing.repository.TepTinRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UploadService - Chủ ngựa (HORSE_OWNER) quản lý tệp tin đính kèm cho TỪNG lần đăng ký thi đấu
 * (loaiDoiTuong = "DANG_KY", maDoiTuong = maDangKy) - ảnh ngựa/nài, hồ sơ sức khỏe, giấy phép...
 * Không còn duyệt file riêng lẻ: Ban tổ chức duyệt cả bộ hồ sơ đăng ký (xem RegistrationService).
 * Sửa/xóa 1 file đã gắn vào đăng ký ĐÃ DUYỆT phải tạo YeuCauCapNhat gửi đúng Ban tổ chức của đăng ký
 * đó chờ duyệt lại (giống luồng sửa Ngựa/Nài - xem UpdateRequestService); file gắn vào đăng ký
 * PENDING/REJECTED/CANCELLED/DISQUALIFIED được sửa/xóa trực tiếp.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UploadService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf");
    private static final String TARGET_TYPE_DANG_KY = "DANG_KY";

    private final TepTinRepository tepTinRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final NguaRepository nguaRepository;
    private final DangKyThiDauRepository dangKyThiDauRepository;
    private final ScheduleRepository scheduleRepository;
    private final MuaGiaiRepository muaGiaiRepository;
    private final UpdateRequestService updateRequestService;
    private final NhatKyHoatDongService nhatKyHoatDongService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public FileUploadResponseDTO createFile(MultipartFile file, TepTinRequestDTO dto, String ownerMaTK, String staffId) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(ownerMaTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", ownerMaTK));
        assertTargetOwnership(dto.getTargetType(), dto.getTargetId(), chuNgua);

        TepTin tepTin = storePhysicalFile(file);
        tepTin.setTenFile(dto.getTenFile() != null && !dto.getTenFile().isBlank() ? dto.getTenFile() : tepTin.getTenFile());
        tepTin.setLoaiFile(dto.getLoaiFile());
        tepTin.setLoaiDoiTuong(dto.getTargetType());
        tepTin.setMaDoiTuong(dto.getTargetId());
        tepTin.setMaTK(ownerMaTK);
        TepTin saved = tepTinRepository.save(tepTin);

        nhatKyHoatDongService.writeAuditLog(staffId, "CREATE_FILE", "File:" + saved.getMaTepTin(),
                "Tải lên tệp tin mới: " + saved.getTenFile());

        return mapToResponseDTO(saved, chuNgua);
    }

    /**
     * Sửa tệp tin:
     * - Đăng ký (DangKyThiDau) sở hữu file này CHƯA hoặc KHÔNG còn APPROVED: cập nhật trực tiếp.
     * - Đăng ký ĐANG APPROVED: tạo YeuCauCapNhat chờ đúng Ban tổ chức đó duyệt lại, dữ liệu gốc giữ nguyên.
     */
    public FileUploadResponseDTO updateFile(String maTepTin, TepTinRequestDTO dto, String staffId) {
        TepTin tepTin = getFileMeta(maTepTin);
        ChuNgua chuNgua = requireOwner(tepTin);
        assertOwnership(chuNgua, staffId);
        assertTargetOwnership(dto.getTargetType(), dto.getTargetId(), chuNgua);

        String organizerId = resolveOrganizerIdIfApproved(tepTin);
        if (organizerId != null) {
            updateRequestService.createForFile(tepTin, dto, chuNgua, organizerId);
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_UPDATE_FILE", "File:" + maTepTin,
                    "Gửi yêu cầu cập nhật tệp tin: " + tepTin.getTenFile());
        } else {
            tepTin.setTenFile(dto.getTenFile());
            tepTin.setLoaiFile(dto.getLoaiFile());
            tepTin.setLoaiDoiTuong(dto.getTargetType());
            tepTin.setMaDoiTuong(dto.getTargetId());
            tepTinRepository.save(tepTin);
            nhatKyHoatDongService.writeAuditLog(staffId, "UPDATE_FILE", "File:" + maTepTin,
                    "Cập nhật trực tiếp tệp tin: " + tepTin.getTenFile());
        }

        return mapToResponseDTO(tepTin, chuNgua);
    }

    /**
     * Xóa tệp tin:
     * - Đăng ký sở hữu file KHÔNG APPROVED: xóa trực tiếp, không cần duyệt.
     * - Đăng ký ĐANG APPROVED: tạo YeuCauCapNhat (hanhDong=XOA) gửi đúng Ban tổ chức đó chờ duyệt trước khi xóa thật.
     */
    public void deleteFile(String maTepTin, String staffId) {
        TepTin tepTin = getFileMeta(maTepTin);
        ChuNgua chuNgua = requireOwner(tepTin);
        assertOwnership(chuNgua, staffId);

        String organizerId = resolveOrganizerIdIfApproved(tepTin);
        if (organizerId != null) {
            updateRequestService.createDeleteRequestForFile(tepTin, chuNgua, organizerId);
            nhatKyHoatDongService.writeAuditLog(staffId, "REQUEST_DELETE_FILE", "File:" + maTepTin,
                    "Gửi yêu cầu xóa tệp tin: " + tepTin.getTenFile());
        } else {
            deletePhysicalFile(tepTin);
            tepTinRepository.delete(tepTin);
            nhatKyHoatDongService.writeAuditLog(staffId, "DELETE_FILE", "File:" + maTepTin,
                    "Đã xóa tệp tin: " + tepTin.getTenFile());
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<FileUploadResponseDTO> listFiles(Pageable pageable, String fileType, String targetType,
            String targetId, String ownerMaTK, String organizerScopeId) {
        List<String> organizerRegistrationIds = organizerScopeId != null ? registrationIdsOfOrganizer(organizerScopeId) : null;

        Specification<TepTin> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fileType != null && !fileType.isBlank()) {
                predicates.add(cb.equal(root.get("loaiFile"), fileType));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("loaiDoiTuong"), targetType));
            }
            if (targetId != null && !targetId.isBlank()) {
                predicates.add(cb.equal(root.get("maDoiTuong"), targetId));
            }
            if (ownerMaTK != null && !ownerMaTK.isBlank()) {
                predicates.add(cb.equal(root.get("maTK"), ownerMaTK));
            }
            if (organizerRegistrationIds != null) {
                predicates.add(organizerRegistrationIds.isEmpty()
                        ? cb.disjunction() : root.get("maDoiTuong").in(organizerRegistrationIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(tepTinRepository.findAll(spec, pageable), this::mapToResponseDTOLazyOwner);
    }

    /** Danh sách maDangKy thuộc các race của 1 Ban tổ chức - dùng để lọc file theo BTC (chỉ thấy file của đăng ký gửi tới mình). */
    private List<String> registrationIdsOfOrganizer(String organizerScopeId) {
        List<String> seasonIds = muaGiaiRepository.findAll((root, query, cb) -> cb.equal(root.get("maBTC"), organizerScopeId))
                .stream().map(MuaGiai::getMaMuaGiai).collect(Collectors.toList());
        if (seasonIds.isEmpty()) {
            return List.of();
        }
        List<String> raceIds = scheduleRepository.findAll((root, query, cb) -> root.get("maMuaGiai").in(seasonIds))
                .stream().map(Schedule::getMaChangDua).collect(Collectors.toList());
        if (raceIds.isEmpty()) {
            return List.of();
        }
        return dangKyThiDauRepository.findAll((root, query, cb) -> root.get("maChangDua").in(raceIds))
                .stream().map(DangKyThiDau::getMaDangKy).collect(Collectors.toList());
    }

    /**
     * Kiểm tra quyền đọc file: chủ sở hữu file luôn được đọc; Ban tổ chức chỉ đọc được file thuộc đăng
     * ký của đúng BTC mình (organizerScopeId - null nghĩa là người gọi không phải ORGANIZER).
     */
    @Transactional(readOnly = true)
    public TepTin getFileForAccess(String maTepTin, String requesterMaTK, String organizerScopeId) {
        TepTin tepTin = getFileMeta(maTepTin);
        ChuNgua chuNgua = tepTin.getMaTK() != null ? chuNguaRepository.findByMaTK(tepTin.getMaTK()).orElse(null) : null;
        boolean isOwner = chuNgua != null && chuNgua.getMaTK().equals(requesterMaTK);
        if (isOwner) {
            return tepTin;
        }
        if (organizerScopeId != null && organizerScopeId.equals(resolveOrganizerId(tepTin))) {
            return tepTin;
        }
        throw new AccessDeniedException("Bạn không có quyền truy cập tệp tin này");
    }

    @Transactional(readOnly = true)
    public FileUploadResponseDTO getFileById(String maTepTin, String requesterMaTK, String organizerScopeId) {
        TepTin tepTin = getFileForAccess(maTepTin, requesterMaTK, organizerScopeId);
        return mapToResponseDTOLazyOwner(tepTin);
    }

    @Transactional(readOnly = true)
    public byte[] readFileBytes(TepTin tepTin) {
        try {
            return Files.readAllBytes(Paths.get(tepTin.getDuongDan()));
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể đọc file: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public TepTin getFileMeta(String maTepTin) {
        return tepTinRepository.findById(maTepTin)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", maTepTin));
    }

    // ----- Helper -----

    private TepTin storePhysicalFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file để tải lên");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File vượt quá dung lượng tối đa cho phép (5MB)");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Định dạng file không được hỗ trợ. Chỉ chấp nhận ảnh (JPG, PNG, GIF, WEBP) hoặc PDF.");
        }

        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);

            String maTepTin = "FT" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
            String storedName = maTepTin + ext;
            Path destination = dir.resolve(storedName);
            file.transferTo(destination);

            return TepTin.builder()
                    .maTepTin(maTepTin)
                    .tenFile(originalName)
                    .duongDan(destination.toString())
                    .contentType(contentType)
                    .kichThuoc(file.getSize())
                    .build();
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể lưu file: " + e.getMessage(), e);
        }
    }

    private void deletePhysicalFile(TepTin tepTin) {
        try {
            Files.deleteIfExists(Paths.get(tepTin.getDuongDan()));
        } catch (IOException e) {
            throw new UncheckedIOException("Không thể xóa file: " + e.getMessage(), e);
        }
    }

    private ChuNgua requireOwner(TepTin tepTin) {
        if (tepTin.getMaTK() == null) {
            throw new ResourceNotFoundException("Chủ sở hữu tệp tin", "maTepTin", tepTin.getMaTepTin());
        }
        return chuNguaRepository.findByMaTK(tepTin.getMaTK())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maTK", tepTin.getMaTK()));
    }

    /** Chặn chủ ngựa A sửa/xóa tệp tin của chủ ngựa B. */
    private void assertOwnership(ChuNgua chuNgua, String requesterMaTK) {
        if (!chuNgua.getMaTK().equals(requesterMaTK)) {
            throw new AccessDeniedException("Bạn không có quyền thao tác trên tệp tin của chủ sở hữu khác");
        }
    }

    /** Chặn chủ ngựa gắn tệp tin của mình vào đăng ký thi đấu của chủ ngựa khác. */
    private void assertTargetOwnership(String targetType, String targetId, ChuNgua chuNgua) {
        if (targetType == null || targetType.isBlank() || targetId == null || targetId.isBlank()) {
            return;
        }
        if (TARGET_TYPE_DANG_KY.equalsIgnoreCase(targetType)) {
            DangKyThiDau registration = dangKyThiDauRepository.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Đăng ký thi đấu", "maDangKy", targetId));
            Ngua ngua = nguaRepository.findById(registration.getMaNgua())
                    .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", registration.getMaNgua()));
            if (!chuNgua.getMaChuNgua().equals(ngua.getMaChuNgua())) {
                throw new AccessDeniedException("Bạn không có quyền gắn tệp tin vào đăng ký thi đấu của chủ sở hữu khác");
            }
        }
    }

    /** Trả về maBTC nếu đăng ký sở hữu file này đang APPROVED (cần duyệt lại khi sửa/xóa), null nếu không. */
    private String resolveOrganizerIdIfApproved(TepTin tepTin) {
        if (!TARGET_TYPE_DANG_KY.equals(tepTin.getLoaiDoiTuong()) || tepTin.getMaDoiTuong() == null) {
            return null;
        }
        DangKyThiDau registration = dangKyThiDauRepository.findById(tepTin.getMaDoiTuong()).orElse(null);
        if (registration == null || !DangKyThiDau.TRANG_THAI_DA_DUYET.equals(registration.getTrangThai())) {
            return null;
        }
        return resolveOrganizerId(tepTin);
    }

    /** Ban tổ chức (maBTC) phụ trách đăng ký sở hữu file này, suy ra qua DangKyThiDau -> Race -> Season. */
    private String resolveOrganizerId(TepTin tepTin) {
        if (!TARGET_TYPE_DANG_KY.equals(tepTin.getLoaiDoiTuong()) || tepTin.getMaDoiTuong() == null) {
            return null;
        }
        return dangKyThiDauRepository.findById(tepTin.getMaDoiTuong())
                .map(DangKyThiDau::getMaChangDua)
                .flatMap(scheduleRepository::findById)
                .map(Schedule::getMaMuaGiai)
                .flatMap(muaGiaiRepository::findById)
                .map(MuaGiai::getMaBTC)
                .orElse(null);
    }

    private FileUploadResponseDTO mapToResponseDTOLazyOwner(TepTin tepTin) {
        ChuNgua chuNgua = tepTin.getMaTK() != null ? chuNguaRepository.findByMaTK(tepTin.getMaTK()).orElse(null) : null;
        return mapToResponseDTO(tepTin, chuNgua);
    }

    private FileUploadResponseDTO mapToResponseDTO(TepTin tepTin, ChuNgua chuNgua) {
        return FileUploadResponseDTO.builder()
                .fileId(tepTin.getMaTepTin())
                .url("/upload/" + tepTin.getMaTepTin())
                .fileName(tepTin.getTenFile())
                .fileType(tepTin.getLoaiFile())
                .fileCategory(tepTin.getLoaiFile())
                .fileSize(tepTin.getKichThuoc() != null ? tepTin.getKichThuoc() : 0L)
                .targetType(tepTin.getLoaiDoiTuong())
                .targetId(tepTin.getMaDoiTuong())
                .ownerId(tepTin.getMaTK())
                .ownerName(chuNgua != null ? chuNgua.getHoTen() : null)
                .createdAt(tepTin.getNgayTao())
                .build();
    }
}
