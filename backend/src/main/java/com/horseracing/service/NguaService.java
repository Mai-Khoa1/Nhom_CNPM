package com.horseracing.service;

import com.horseracing.dto.horse.HorseRequestDTO;
import com.horseracing.dto.horse.HorseResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.entity.Ngua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NguaRepository;
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
 * HorseService - Business logic quản lý ngựa đua
 * Theo sơ đồ hoạt động và sơ đồ tuần tự đã thiết kế
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HorseService {

    private final NguaRepository nguaRepository;
    private final ChuNguaRepository chuNguaRepository;

    // ===================== THÊM MỚI =====================

    /**
     * Tạo mới ngựa đua
     * Bước 5: Validate → 7b: Kiểm tra trùng lặp → 8b: INSERT INTO Ngua → trả về HorseResponseDTO
     */
    public HorseResponseDTO createHorse(HorseRequestDTO dto) {
        log.info("Đang tạo ngựa mới: {}", dto.getTenNgua());

        // Kiểm tra chủ ngựa tồn tại
        ChuNgua chuNgua = chuNguaRepository.findById(dto.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", dto.getMaChuNgua()));

        // Tạo entity mới
        Ngua ngua = Ngua.builder()
                .maNgua(generateMaNgua())
                .maChuNgua(dto.getMaChuNgua())
                .tenNgua(dto.getTenNgua())
                .giongNgua(dto.getGiongNgua())
                .ngaySinh(parseDate(dto.getNgaySinh()))
                .gioiTinh(dto.getGioiTinh() != null ? Ngua.GioiTinh.valueOf(dto.getGioiTinh()) : null)
                .mauLong(dto.getMauLong())
                .troiLuong(dto.getTroiLuong())
                .trangThaiSucKhoe(dto.getTrangThaiSucKhoe())
                .trangThai(dto.getTrangThai() != null ? dto.getTrangThai() : "Chờ duyệt")
                .build();

        Ngua saved = nguaRepository.save(ngua);
        log.info("Đã tạo ngựa thành công: {}", saved.getMaNgua());
        return mapToResponseDTO(saved, chuNgua.getHoTen());
    }

    // ===================== CẬP NHẬT =====================

    /**
     * Cập nhật thông tin ngựa
     */
    public HorseResponseDTO updateHorse(String maNgua, HorseRequestDTO dto) {
        log.info("Cập nhật ngựa: {}", maNgua);

        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));

        ChuNgua chuNgua = chuNguaRepository.findById(dto.getMaChuNgua())
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", dto.getMaChuNgua()));

        // Cập nhật các trường
        ngua.setTenNgua(dto.getTenNgua());
        ngua.setGiongNgua(dto.getGiongNgua());
        ngua.setNgaySinh(parseDate(dto.getNgaySinh()));
        ngua.setGioiTinh(dto.getGioiTinh() != null ? Ngua.GioiTinh.valueOf(dto.getGioiTinh()) : null);
        ngua.setMauLong(dto.getMauLong());
        ngua.setTroiLuong(dto.getTroiLuong());
        ngua.setTrangThaiSucKhoe(dto.getTrangThaiSucKhoe());
        if (dto.getTrangThai() != null) {
            ngua.setTrangThai(dto.getTrangThai());
        }
        ngua.setMaChuNgua(dto.getMaChuNgua());

        Ngua updated = nguaRepository.save(ngua);
        log.info("Cập nhật ngựa thành công: {}", maNgua);
        return mapToResponseDTO(updated, chuNgua.getHoTen());
    }

    // ===================== XÓA =====================

    /**
     * Xóa ngựa - kiểm tra lịch đua trước khi xóa
     * Theo sơ đồ: "Ngựa có lịch đua?" → yes: Thông báo không thể xóa
     */
    public void deleteHorse(String maNgua) {
        log.info("Yêu cầu xóa ngựa: {}", maNgua);

        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));

        // Kiểm tra lịch đua
        if (nguaRepository.hasUpcomingRaces(maNgua)) {
            throw new ResourceInUseException(
                    "Không thể xóa ngựa '" + ngua.getTenNgua() + "' vì đang có lịch thi đấu sắp diễn ra.");
        }

        nguaRepository.delete(ngua);
        log.info("Đã xóa ngựa: {}", maNgua);
    }

    // ===================== XEM / TÌM KIẾM =====================

    /**
     * Lấy tất cả ngựa
     */
    @Transactional(readOnly = true)
    public List<HorseResponseDTO> getAllHorses() {
        return nguaRepository.findAll().stream()
                .map(ngua -> {
                    String tenChuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                            .map(ChuNgua::getHoTen).orElse("N/A");
                    return mapToResponseDTO(ngua, tenChuNgua);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy ngựa theo ID
     */
    @Transactional(readOnly = true)
    public HorseResponseDTO getHorseById(String maNgua) {
        Ngua ngua = nguaRepository.findById(maNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Ngựa", "maNgua", maNgua));
        String tenChuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                .map(ChuNgua::getHoTen).orElse("N/A");
        return mapToResponseDTO(ngua, tenChuNgua);
    }

    /**
     * Tìm kiếm + lọc ngựa
     * Theo sơ đồ: "Nhập từ khóa/bộ lọc → Truy vấn CSDL → Hiển thị kết quả"
     */
    @Transactional(readOnly = true)
    public List<HorseResponseDTO> searchHorses(String tenNgua, String trangThai, String maChuNgua) {
        List<Ngua> result;

        if (maChuNgua != null && !maChuNgua.isBlank()) {
            result = nguaRepository.findByMaChuNgua(maChuNgua);
        } else if (tenNgua != null && !tenNgua.isBlank() && trangThai != null && !trangThai.isBlank()) {
            result = nguaRepository.findByTenNguaContainingIgnoreCaseAndTrangThai(tenNgua, trangThai);
        } else if (tenNgua != null && !tenNgua.isBlank()) {
            result = nguaRepository.findByTenNguaContainingIgnoreCase(tenNgua);
        } else if (trangThai != null && !trangThai.isBlank()) {
            result = nguaRepository.findByTrangThai(trangThai);
        } else {
            result = nguaRepository.findAll();
        }

        return result.stream()
                .map(ngua -> {
                    String tenChuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                            .map(ChuNgua::getHoTen).orElse("N/A");
                    return mapToResponseDTO(ngua, tenChuNgua);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy ngựa theo chủ ngựa
     */
    @Transactional(readOnly = true)
    public List<HorseResponseDTO> getHorsesByOwner(String maChuNgua) {
        chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));
        return nguaRepository.findByMaChuNgua(maChuNgua).stream()
                .map(ngua -> {
                    String tenChuNgua = chuNguaRepository.findById(ngua.getMaChuNgua())
                            .map(ChuNgua::getHoTen).orElse("N/A");
                    return mapToResponseDTO(ngua, tenChuNgua);
                })
                .collect(Collectors.toList());
    }

    // ===================== HELPER =====================

    private HorseResponseDTO mapToResponseDTO(Ngua ngua, String tenChuNgua) {
        return HorseResponseDTO.builder()
                .maNgua(ngua.getMaNgua())
                .tenNgua(ngua.getTenNgua())
                .giongNgua(ngua.getGiongNgua())
                .ngaySinh(ngua.getNgaySinh())
                .gioiTinh(ngua.getGioiTinh() != null ? ngua.getGioiTinh().name() : null)
                .mauLong(ngua.getMauLong())
                .troiLuong(ngua.getTroiLuong())
                .trangThaiSucKhoe(ngua.getTrangThaiSucKhoe())
                .trangThai(ngua.getTrangThai())
                .maChuNgua(ngua.getMaChuNgua())
                .tenChuNgua(tenChuNgua)
                .ngayTao(ngua.getNgayTao())
                .ngayCapNhat(ngua.getNgayCapNhat())
                .build();
    }

    private String generateMaNgua() {
        return "N" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
