package com.horseracing.service;

import com.horseracing.dto.jockey.JockeyRequestDTO;
import com.horseracing.dto.jockey.JockeyResponseDTO;
import com.horseracing.dto.jockey.JockeyStatsDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.NaiNgua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JockeyService - Business logic quản lý nài ngựa (Jockey)
 * Theo sơ đồ hoạt động và sơ đồ tuần tự đã thiết kế
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JockeyService {

    private final NaiNguaRepository naiNguaRepository;
    private final ChuNguaRepository chuNguaRepository;
    private final AuditLogService auditLogService;

    // ===================== THÊM MỚI =====================

    /**
     * Tạo mới jockey - kiểm tra trùng lặp CCCD/giấy phép trước khi lưu
     * Theo sơ đồ: "Kiểm tra trùng lặp CCCD" → "Đã tồn tại?" → yes: Báo lỗi | no: Lưu vào CSDL
     */
    public JockeyResponseDTO createJockey(JockeyRequestDTO dto, String staffId) {
        log.info("Đang tạo jockey mới: {}", dto.getHoTen());

        // Kiểm tra chủ ngựa tồn tại
        ChuNgua chuNgua = chuNguaRepository.findById(dto.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", dto.getMaChuNgua()));

        // Kiểm tra trùng số giấy phép
        if (naiNguaRepository.existsBySoGiayPhep(dto.getSoGiayPhep())) {
            throw new DuplicateResourceException(
                    "Số giấy phép '" + dto.getSoGiayPhep() + "' đã tồn tại trong hệ thống.");
        }

        NaiNgua naiNgua = NaiNgua.builder()
                .maNaiNgua(generateMaNaiNgua())
                .maChuNgua(dto.getMaChuNgua())
                .hoTen(dto.getHoTen())
                .ngaySinh(parseDate(dto.getNgaySinh()))
                .quocTich(dto.getQuocTich())
                .kinhNghiem(dto.getKinhNghiem())
                .soGiayPhep(dto.getSoGiayPhep())
                .trangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "Sẵn sàng")
                .build();

        NaiNgua saved = naiNguaRepository.save(naiNgua);

        // Ghi audit log
        auditLogService.writeAuditLog(
                staffId, "CREATE_JOCKEY",
                "Jockey:" + saved.getMaNaiNgua(),
                "Tạo jockey mới: " + saved.getHoTen()
        );

        log.info("Đã tạo jockey thành công: {}", saved.getMaNaiNgua());
        return mapToResponseDTO(saved, chuNgua.getHoTen());
    }

    // ===================== CẬP NHẬT =====================

    /**
     * Cập nhật thông tin jockey
     */
    public JockeyResponseDTO updateJockey(String maNaiNgua, JockeyRequestDTO dto, String staffId) {
        log.info("Cập nhật jockey: {}", maNaiNgua);

        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        ChuNgua chuNgua = chuNguaRepository.findById(dto.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", dto.getMaChuNgua()));

        // Kiểm tra trùng số giấy phép với jockey khác
        if (naiNguaRepository.existsBySoGiayPhepAndMaNaiNguaNot(dto.getSoGiayPhep(), maNaiNgua)) {
            throw new DuplicateResourceException(
                    "Số giấy phép '" + dto.getSoGiayPhep() + "' đã được sử dụng bởi jockey khác.");
        }

        naiNgua.setHoTen(dto.getHoTen());
        naiNgua.setNgaySinh(parseDate(dto.getNgaySinh()));
        naiNgua.setQuocTich(dto.getQuocTich());
        naiNgua.setKinhNghiem(dto.getKinhNghiem());
        naiNgua.setSoGiayPhep(dto.getSoGiayPhep());
        naiNgua.setMaChuNgua(dto.getMaChuNgua());
        if (dto.getTrangThai() != null) {
            naiNgua.setTrangThai(dto.getTrangThai());
        }

        NaiNgua updated = naiNguaRepository.save(naiNgua);

        auditLogService.writeAuditLog(
                staffId, "UPDATE_JOCKEY",
                "Jockey:" + maNaiNgua,
                "Cập nhật thông tin jockey: " + updated.getHoTen()
        );

        log.info("Cập nhật jockey thành công: {}", maNaiNgua);
        return mapToResponseDTO(updated, chuNgua.getHoTen());
    }

    /**
     * Cập nhật chỉ số sức khỏe / thống kê của jockey
     * Theo sơ đồ tuần tự: validateAndUpdate → UPDATE jockeys SET → writeAuditLog → trả về JockeyUpdatedDTO
     */
    public JockeyResponseDTO updateStats(String maNaiNgua, JockeyStatsDTO statsDTO, String staffId) {
        log.info("Cập nhật chỉ số jockey: {}", maNaiNgua);

        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        // Cập nhật chỉ số
        if (statsDTO.getCanNang() != null) naiNgua.setCanNang(statsDTO.getCanNang());
        if (statsDTO.getBmi() != null) naiNgua.setBmi(statsDTO.getBmi());
        if (statsDTO.getTyLeThang() != null) naiNgua.setTyLeThang(statsDTO.getTyLeThang());
        if (statsDTO.getGhiChu() != null) naiNgua.setGhiChu(statsDTO.getGhiChu());

        NaiNgua updated = naiNguaRepository.save(naiNgua);

        // Ghi audit log: writeAuditLog(jockeyId, "UPDATE_STATS", staffId)
        auditLogService.writeAuditLog(
                staffId, "UPDATE_STATS",
                "Jockey:" + maNaiNgua,
                String.format("Cập nhật chỉ số jockey %s - Cân nặng: %s, BMI: %s, Tỷ lệ thắng: %s%%",
                        naiNgua.getHoTen(), statsDTO.getCanNang(), statsDTO.getBmi(), statsDTO.getTyLeThang())
        );

        log.info("Cập nhật chỉ số jockey thành công: {}", maNaiNgua);

        String tenChuNgua = chuNguaRepository.findById(updated.getMaChuNgua())
                .map(ChuNgua::getHoTen).orElse("N/A");
        return mapToResponseDTO(updated, tenChuNgua);
    }

    // ===================== XÓA =====================

    /**
     * Xóa jockey - kiểm tra lịch đua sắp tới
     * Theo sơ đồ: "Có lịch đua sắp tới?" → yes: Chỉ vô hiệu hóa | no: Xóa khỏi CSDL
     */
    public void deleteJockey(String maNaiNgua, String staffId) {
        log.info("Yêu cầu xóa jockey: {}", maNaiNgua);

        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));

        // Kiểm tra lịch đua sắp tới
        if (naiNguaRepository.hasUpcomingRaces(maNaiNgua)) {
            // Có lịch đua → chỉ vô hiệu hóa (Inactive)
            naiNgua.setTrangThai("Nghỉ hưu");
            naiNguaRepository.save(naiNgua);

            auditLogService.writeAuditLog(
                    staffId, "DEACTIVATE_JOCKEY",
                    "Jockey:" + maNaiNgua,
                    "Vô hiệu hóa jockey " + naiNgua.getHoTen() + " (đang có lịch thi đấu)"
            );

            throw new ResourceInUseException(
                    "Jockey '" + naiNgua.getHoTen() + "' đang có lịch thi đấu sắp tới. " +
                    "Hệ thống đã chuyển trạng thái thành 'Nghỉ hưu' thay vì xóa.");
        }

        naiNguaRepository.delete(naiNgua);

        auditLogService.writeAuditLog(
                staffId, "DELETE_JOCKEY",
                "Jockey:" + maNaiNgua,
                "Đã xóa jockey: " + naiNgua.getHoTen()
        );

        log.info("Đã xóa jockey: {}", maNaiNgua);
    }

    // ===================== XEM / TÌM KIẾM / THỐNG KÊ =====================

    /**
     * Lấy tất cả jockey
     */
    @Transactional(readOnly = true)
    public List<JockeyResponseDTO> getAllJockeys() {
        return naiNguaRepository.findAll().stream()
                .map(j -> {
                    String tenChuNgua = chuNguaRepository.findById(j.getMaChuNgua())
                            .map(ChuNgua::getHoTen).orElse("N/A");
                    return mapToResponseDTO(j, tenChuNgua);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy jockey theo ID
     */
    @Transactional(readOnly = true)
    public JockeyResponseDTO getJockeyById(String maNaiNgua) {
        NaiNgua naiNgua = naiNguaRepository.findById(maNaiNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey", "maNaiNgua", maNaiNgua));
        String tenChuNgua = chuNguaRepository.findById(naiNgua.getMaChuNgua())
                .map(ChuNgua::getHoTen).orElse("N/A");
        return mapToResponseDTO(naiNgua, tenChuNgua);
    }

    /**
     * Tìm kiếm + lọc jockey
     * Theo sơ đồ: "Lọc theo tên, trạng thái, kinh nghiệm → Truy vấn CSDL → Hiển thị danh sách + số liệu"
     */
    @Transactional(readOnly = true)
    public List<JockeyResponseDTO> searchJockeys(String hoTen, String trangThai, Integer kinhNghiemMin) {
        List<NaiNgua> result;

        boolean hasName = hoTen != null && !hoTen.isBlank();
        boolean hasStatus = trangThai != null && !trangThai.isBlank();
        boolean hasExp = kinhNghiemMin != null;

        if (hasName && hasStatus && hasExp) {
            result = naiNguaRepository
                    .findByHoTenContainingIgnoreCaseAndTrangThaiAndKinhNghiemGreaterThanEqual(
                            hoTen, trangThai, kinhNghiemMin);
        } else if (hasName && hasStatus) {
            result = naiNguaRepository.findByHoTenContainingIgnoreCase(hoTen).stream()
                    .filter(j -> j.getTrangThai().equals(trangThai))
                    .collect(Collectors.toList());
        } else if (hasName) {
            result = naiNguaRepository.findByHoTenContainingIgnoreCase(hoTen);
        } else if (hasStatus) {
            result = naiNguaRepository.findByTrangThai(trangThai);
        } else if (hasExp) {
            result = naiNguaRepository.findByKinhNghiemGreaterThanEqual(kinhNghiemMin);
        } else {
            result = naiNguaRepository.findAll();
        }

        return result.stream()
                .map(j -> {
                    String tenChuNgua = chuNguaRepository.findById(j.getMaChuNgua())
                            .map(ChuNgua::getHoTen).orElse("N/A");
                    return mapToResponseDTO(j, tenChuNgua);
                })
                .collect(Collectors.toList());
    }

    // ===================== HELPER =====================

    private JockeyResponseDTO mapToResponseDTO(NaiNgua naiNgua, String tenChuNgua) {
        return JockeyResponseDTO.builder()
                .maNaiNgua(naiNgua.getMaNaiNgua())
                .hoTen(naiNgua.getHoTen())
                .ngaySinh(naiNgua.getNgaySinh())
                .quocTich(naiNgua.getQuocTich())
                .kinhNghiem(naiNgua.getKinhNghiem())
                .soGiayPhep(naiNgua.getSoGiayPhep())
                .trangThai(naiNgua.getTrangThai())
                .maChuNgua(naiNgua.getMaChuNgua())
                .tenChuNgua(tenChuNgua)
                .ngayTao(naiNgua.getNgayTao())
                .ngayCapNhat(naiNgua.getNgayCapNhat())
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
