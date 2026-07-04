package com.horseracing.service;

import com.horseracing.dto.chungua.ChuNguaRequestDTO;
import com.horseracing.dto.chungua.ChuNguaResponseDTO;
import com.horseracing.entity.ChuNgua;
import com.horseracing.exception.DuplicateResourceException;
import com.horseracing.exception.ResourceInUseException;
import com.horseracing.exception.ResourceNotFoundException;
import com.horseracing.repository.ChuNguaRepository;
import com.horseracing.repository.NaiNguaRepository;
import com.horseracing.repository.NguaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ChuNguaService - quản lý hồ sơ chủ ngựa (mở rộng 1-1 từ tài khoản TaiKhoan có vaiTro = "Chủ ngựa").
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChuNguaService {

    private final ChuNguaRepository chuNguaRepository;
    private final NguaRepository nguaRepository;
    private final NaiNguaRepository naiNguaRepository;

    public ChuNguaResponseDTO createChuNgua(ChuNguaRequestDTO dto, String maTK) {
        if (chuNguaRepository.findByMaTK(maTK).isPresent()) {
            throw new DuplicateResourceException("Tài khoản này đã có hồ sơ chủ ngựa");
        }

        ChuNgua chuNgua = ChuNgua.builder()
                .maChuNgua(generateMaChuNgua())
                .maTK(maTK)
                .hoTen(dto.getHoTen())
                .diaChi(dto.getDiaChi())
                .soDienThoai(dto.getSoDienThoai())
                .email(dto.getEmail())
                .build();

        return mapToResponseDTO(chuNguaRepository.save(chuNgua));
    }

    public ChuNguaResponseDTO updateChuNgua(String maChuNgua, ChuNguaRequestDTO dto, String requesterMaTK, boolean privileged) {
        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));
        assertOwnership(chuNgua, requesterMaTK, privileged);

        chuNgua.setHoTen(dto.getHoTen());
        chuNgua.setDiaChi(dto.getDiaChi());
        chuNgua.setSoDienThoai(dto.getSoDienThoai());
        chuNgua.setEmail(dto.getEmail());

        return mapToResponseDTO(chuNguaRepository.save(chuNgua));
    }

    public void deleteChuNgua(String maChuNgua, String requesterMaTK, boolean privileged) {
        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));
        assertOwnership(chuNgua, requesterMaTK, privileged);

        boolean hasHorses = !nguaRepository.findByMaChuNgua(maChuNgua).isEmpty();
        boolean hasJockeys = !naiNguaRepository.findByMaChuNgua(maChuNgua).isEmpty();
        if (hasHorses || hasJockeys) {
            throw new ResourceInUseException(
                    "Không thể xóa chủ ngựa '" + chuNgua.getHoTen() + "' vì còn ngựa hoặc jockey thuộc sở hữu.");
        }

        chuNguaRepository.delete(chuNgua);
    }

    @Transactional(readOnly = true)
    public List<ChuNguaResponseDTO> getAllChuNgua() {
        return chuNguaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChuNguaResponseDTO getChuNguaById(String maChuNgua) {
        ChuNgua chuNgua = chuNguaRepository.findById(maChuNgua)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa", "maChuNgua", maChuNgua));
        return mapToResponseDTO(chuNgua);
    }

    @Transactional(readOnly = true)
    public ChuNguaResponseDTO getChuNguaByMaTK(String maTK) {
        ChuNgua chuNgua = chuNguaRepository.findByMaTK(maTK)
                .orElseThrow(() -> new ResourceNotFoundException("Chủ ngựa của tài khoản", "maTK", maTK));
        return mapToResponseDTO(chuNgua);
    }

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

    private void assertOwnership(ChuNgua chuNgua, String requesterMaTK, boolean privileged) {
        if (!privileged && !chuNgua.getMaTK().equals(requesterMaTK)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Bạn không có quyền thao tác trên hồ sơ chủ ngựa khác");
        }
    }

    private String generateMaChuNgua() {
        return "CN" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
