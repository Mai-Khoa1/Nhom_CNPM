package com.horseracing.service;

import com.horseracing.dto.nhatky.NhatKyHoatDongResponseDTO;
import com.horseracing.entity.NhatKyHoatDong;
import com.horseracing.repository.NhatKyHoatDongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ghi và truy vấn nhật ký hoạt động (audit log) của hệ thống.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NhatKyHoatDongService {

    private final NhatKyHoatDongRepository nhatKyHoatDongRepository;

    public void writeAuditLog(String maTK, String loaiHanhDong, String doiTuongTacDong, String moTa) {
        NhatKyHoatDong log = NhatKyHoatDong.builder()
                .maNhatKy(generateMaNhatKy())
                .maTK(maTK)
                .loaiHanhDong(loaiHanhDong)
                .doiTuongTacDong(doiTuongTacDong)
                .moTa(moTa)
                .build();
        nhatKyHoatDongRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<NhatKyHoatDongResponseDTO> getAllLogs() {
        return nhatKyHoatDongRepository.findAllByOrderByThoiGianDesc().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NhatKyHoatDongResponseDTO> getLogsByUser(String maTK) {
        return nhatKyHoatDongRepository.findByMaTKOrderByThoiGianDesc(maTK).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NhatKyHoatDongResponseDTO> getLogsByAction(String loaiHanhDong) {
        return nhatKyHoatDongRepository.findByLoaiHanhDongOrderByThoiGianDesc(loaiHanhDong).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private NhatKyHoatDongResponseDTO mapToResponseDTO(NhatKyHoatDong log) {
        return NhatKyHoatDongResponseDTO.builder()
                .maNhatKy(log.getMaNhatKy())
                .maTK(log.getMaTK())
                .loaiHanhDong(log.getLoaiHanhDong())
                .moTa(log.getMoTa())
                .thoiGian(log.getThoiGian())
                .diaChiIP(log.getDiaChiIP())
                .doiTuongTacDong(log.getDoiTuongTacDong())
                .build();
    }

    private String generateMaNhatKy() {
        return "NK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
