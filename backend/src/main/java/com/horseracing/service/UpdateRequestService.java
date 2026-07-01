package com.horseracing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.horseracing.dto.common.*;
import com.horseracing.dto.nainghua.NaiNguaRequestDTO;
import com.horseracing.dto.ngua.NguaRequestDTO;
import com.horseracing.dto.updaterequest.UpdateRequestResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.NaiNgua;
import com.horseracing.entity.Ngua;
import com.horseracing.entity.YeuCauCapNhat;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
import com.horseracing.repository.NguaRepository;
import com.horseracing.repository.YeuCauCapNhatRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ChuNguaRepository chuNguaRepository;
    private final NotificationService notificationService;
    private final NhatKyHoatDongService nhatKyHoatDongService;
    private final ObjectMapper objectMapper;

    public void createForHorse(Ngua current, NguaRequestDTO newData, ChuNgua owner) {
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
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(newData))
                .build();
        updateRequestRepository.save(request);

        notificationService.notifyAdmins(
                "Yêu cầu cập nhật thông tin ngựa",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu cập nhật thông tin ngựa '" + current.getTenNgua() + "', cần duyệt.",
                NotificationType.SYSTEM, "HORSE_UPDATE_REQUEST", request.getMaYeuCau());
    }

    public void createForJockey(NaiNgua current, NaiNguaRequestDTO newData, ChuNgua owner) {
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
                .duLieuCu(toJson(oldData))
                .duLieuMoi(toJson(newData))
                .build();
        updateRequestRepository.save(request);

        notificationService.notifyAdmins(
                "Yêu cầu cập nhật thông tin nài ngựa",
                "Chủ ngựa " + owner.getHoTen() + " yêu cầu cập nhật thông tin nài '" + current.getHoTen() + "', cần duyệt.",
                NotificationType.SYSTEM, "JOCKEY_UPDATE_REQUEST", request.getMaYeuCau());
    }

    public UpdateRequestResponseDTO approve(String maYeuCau, String staffId) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        requirePending(request);

        if (YeuCauCapNhat.LOAI_NGUA.equals(request.getLoaiDoiTuong())) {
            applyHorseUpdate(request);
        } else {
            applyJockeyUpdate(request);
        }

        request.setTrangThai(YeuCauCapNhat.TRANG_THAI_DA_DUYET);
        request.setNgayXuLy(LocalDateTime.now());
        YeuCauCapNhat updated = updateRequestRepository.save(request);

        nhatKyHoatDongService.writeAuditLog(staffId, "APPROVE_UPDATE_REQUEST", "UpdateRequest:" + maYeuCau,
                "Đã duyệt yêu cầu cập nhật " + request.getLoaiDoiTuong() + ":" + request.getMaDoiTuong());

        notificationService.notify(updated.getMaTK(),
                "Yêu cầu cập nhật đã được duyệt",
                "Thông tin cập nhật của bạn đã được Ban tổ chức duyệt và áp dụng.",
                NotificationType.APPROVAL, targetTypeLabel(updated), updated.getMaDoiTuong());

        return mapToResponseDTO(updated);
    }

    public UpdateRequestResponseDTO reject(String maYeuCau, String reason, String staffId) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        requirePending(request);

        request.setTrangThai(YeuCauCapNhat.TRANG_THAI_TU_CHOI);
        request.setLyDoTuChoi(reason);
        request.setNgayXuLy(LocalDateTime.now());
        YeuCauCapNhat updated = updateRequestRepository.save(request);

        nhatKyHoatDongService.writeAuditLog(staffId, "REJECT_UPDATE_REQUEST", "UpdateRequest:" + maYeuCau,
                "Đã từ chối yêu cầu cập nhật " + request.getLoaiDoiTuong() + ":" + request.getMaDoiTuong()
                        + (reason != null && !reason.isBlank() ? " (Lý do: " + reason + ")" : ""));

        notificationService.notify(updated.getMaTK(),
                "Yêu cầu cập nhật bị từ chối",
                "Thông tin cập nhật của bạn đã bị Ban tổ chức từ chối"
                        + (reason != null && !reason.isBlank() ? ": " + reason : ".") + " Vui lòng sửa lại và gửi lại.",
                NotificationType.REJECTION, targetTypeLabel(updated), updated.getMaDoiTuong());

        return mapToResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public UpdateRequestResponseDTO getById(String maYeuCau) {
        YeuCauCapNhat request = updateRequestRepository.findById(maYeuCau)
                .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu cập nhật", "id", maYeuCau));
        return mapToResponseDTO(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<UpdateRequestResponseDTO> getAll(Pageable pageable, UpdateRequestStatus status, UpdateTargetType targetType) {
        Specification<YeuCauCapNhat> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("trangThai"), toTrangThai(status)));
            }
            if (targetType != null) {
                predicates.add(cb.equal(root.get("loaiDoiTuong"), toLoaiDoiTuong(targetType)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return PageResponse.of(updateRequestRepository.findAll(spec, pageable), this::mapToResponseDTO);
    }

    // ----- Helper -----

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

    private UpdateRequestResponseDTO mapToResponseDTO(YeuCauCapNhat request) {
        boolean isHorse = YeuCauCapNhat.LOAI_NGUA.equals(request.getLoaiDoiTuong());
        Map<String, Object> oldData = fromJsonToMap(request.getDuLieuCu());
        Map<String, Object> newData = fromJsonToMap(request.getDuLieuMoi());
        String targetName = (String) oldData.getOrDefault(isHorse ? "name" : "fullName", request.getMaDoiTuong());
        ChuNgua owner = chuNguaRepository.findByMaTK(request.getMaTK()).orElse(null);

        return UpdateRequestResponseDTO.builder()
                .id(request.getMaYeuCau())
                .targetType(isHorse ? UpdateTargetType.HORSE : UpdateTargetType.JOCKEY)
                .targetId(request.getMaDoiTuong())
                .targetName(targetName)
                .ownerId(request.getMaTK())
                .ownerName(owner != null ? owner.getHoTen() : null)
                .oldData(oldData)
                .newData(newData)
                .status(StatusMapper.toUpdateRequestStatus(request.getTrangThai()))
                .rejectReason(request.getLyDoTuChoi())
                .createdAt(request.getNgayTao())
                .processedAt(request.getNgayXuLy())
                .build();
    }

    private String targetTypeLabel(YeuCauCapNhat request) {
        return YeuCauCapNhat.LOAI_NGUA.equals(request.getLoaiDoiTuong()) ? "HORSE" : "JOCKEY";
    }

    private String toTrangThai(UpdateRequestStatus status) {
        return switch (status) {
            case PENDING -> YeuCauCapNhat.TRANG_THAI_CHO_DUYET;
            case APPROVED -> YeuCauCapNhat.TRANG_THAI_DA_DUYET;
            case REJECTED -> YeuCauCapNhat.TRANG_THAI_TU_CHOI;
        };
    }

    private String toLoaiDoiTuong(UpdateTargetType targetType) {
        return targetType == UpdateTargetType.HORSE ? YeuCauCapNhat.LOAI_NGUA : YeuCauCapNhat.LOAI_NAI_NGUA;
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
