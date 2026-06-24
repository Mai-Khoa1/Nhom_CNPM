package com.horseracing.service;

import com.horseracing.dto.chungua.ChuNguaRequestDTO;
import com.horseracing.dto.chungua.ChuNguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NguaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChuNguaService - Business logic quản lý chủ ngựa
 * Theo sơ đồ hoạt động và sơ đồ tuần tự đã thiết kế
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChuNguaService {

    private final ChuNguaRepository chuNguaRepository;
    private final NguaRepository nguaRepository;

    // ===================== THÊM MỚI =====================

    /**
     * Tạo mới chủ ngựa
     */
    public ChuNguaResponseDTO createChuNgua(ChuNguaRequestDTO dto) {
        log.info("Đang tạo chủ ngựa mới: {}", dto.getHoTen());

        // Kiểm tra trùng lặp mã chủ ngựa
        if (chuNguaRepository.existsById(dto.getMaChuNgua())) {
            throw new DuplicateResourceException("Chủ ngựa", "maChuNgua", dto.getMaChuNgua());
        }

        // Kiểm tra trùng lặp mã tài khoản
        if (chuNguaRepository.existsByMaTK(dto.getMaTK())) {
            throw new DuplicateResourceException("Tài khoản", "maTK", dto.getMaTK());
        }

        // Kiểm tra trùng lặp email
        if (chuNguaRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email", "email", dto.getEmail());
        }

        // Tạo entity mới
        ChuNgua chuNgua = ChuNgua.builder()
                .maChuNgua(dto.getMaChuNgua())
                .maTK(dto.getMaTK())
                .hoTen(dto.getHoTen())
                .diaChi(dto.getDiaChi())
                .soDienThoai(dto.getSoDienThoai())
                .email(dto.getEmail())
                .build();

        ChuNgua saved = chuNguaRepository.save(chuNgua);
        log.info("Đã tạo chủ ngựa thành công: {}", saved.getMaChuNgua());
        return mapToResponseDTO(saved);
    }

    // ===================== CẬP NHẬT =====================

    /**
     * Cập nhật thông tin chủ ngựa
     */
    public ChuNguaResponseDTO updateChuNgua(String maChuNgua, ChuNguaRequestDTO dto) {
        log.info("Cập nhật chủ ngựa: {}", maChuNgua);

        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));

        // Kiểm tra trùng lặp email nếu thay đổi
        if (!chuNgua.getEmail().equals(dto.getEmail()) && chuNguaRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email", "email", dto.getEmail());
        }

        // Cập nhật các trường
        chuNgua.setHoTen(dto.getHoTen());
        chuNgua.setDiaChi(dto.getDiaChi());
        chuNgua.setSoDienThoai(dto.getSoDienThoai());
        chuNgua.setEmail(dto.getEmail());

        ChuNgua updated = chuNguaRepository.save(chuNgua);
        log.info("Cập nhật chủ ngựa thành công: {}", maChuNgua);
        return mapToResponseDTO(updated);
    }

    // ===================== XÓA =====================

    /**
     * Xóa chủ ngựa - kiểm tra xem có ngựa nào thuộc chủ này không
     */
    public void deleteChuNgua(String maChuNgua) {
        log.info("Yêu cầu xóa chủ ngựa: {}", maChuNgua);

        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));

        // Kiểm tra xem có ngựa nào thuộc chủ này không
        if (nguaRepository.existsByMaChuNgua(maChuNgua)) {
            throw new ResourceInUseException(
                    "Không thể xóa chủ ngựa '" + chuNgua.getHoTen() + "' vì đang có ngựa thuộc chủ này.");
        }

        chuNguaRepository.delete(chuNgua);
        log.info("Đã xóa chủ ngựa: {}", maChuNgua);
    }

    // ===================== XEM / TÌM KIẾM =====================

    /**
     * Lấy tất cả chủ ngựa
     */
    @Transactional(readOnly = true)
    public List<ChuNguaResponseDTO> getAllChuNgua() {
        return chuNguaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chủ ngựa theo ID
     */
    @Transactional(readOnly = true)
    public ChuNguaResponseDTO getChuNguaById(String maChuNgua) {
        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));
        return mapToResponseDTO(chuNgua);
    }

    /**
     * Tìm kiếm chủ ngựa theo họ tên
     */
    @Transactional(readOnly = true)
    public List<ChuNguaResponseDTO> searchChuNgua(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) {
            return getAllChuNgua();
        }
        return chuNguaRepository.findByHoTenContainingIgnoreCase(hoTen).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chủ ngựa theo email
     */
    @Transactional(readOnly = true)
    public ChuNguaResponseDTO getChuNguaByEmail(String email) {
        ChuNgua chuNgua = chuNguaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "email", email));
        return mapToResponseDTO(chuNgua);
    }

    // ===================== HELPER =====================

    private ChuNguaResponseDTO mapToResponseDTO(ChuNgua chuNgua) {
        return ChuNguaResponseDTO.builder()
                .maChuNgua(chuNgua.getMaChuNgua())
                .maTK(chuNgua.getMaTK())
                .hoTen(chuNgua.getHoTen())
                .diaChi(chuNgua.getDiaChi())
                .soDienThoai(chuNgua.getSoDienThoai())
                .email(chuNgua.getEmail())
                .build();
    }
}
