package com.horseracing.service;

import com.horseracing.entity.NhatKyHoatDong;
import com.horseracing.repository.NhatKyHoatDongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuditLogService - Ghi nhật ký hoạt động
 * Theo sơ đồ tuần tự jockey: writeAuditLog(jockeyId, "UPDATE_STATS", staffId)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final NhatKyHoatDongRepository nhatKyRepository;

    /**
     * Ghi log hành động - chạy trong transaction mới để không bị rollback theo caller
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAuditLog(String maTK, String loaiHanhDong, String doiTuong, String moTa) {
        try {
            NhatKyHoatDong log = NhatKyHoatDong.builder()
                    .maNhatKy("NK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                    .maTK(maTK)
                    .loaiHanhDong(loaiHanhDong)
                    .doiTuongTacDong(doiTuong)
                    .moTa(moTa)
                    .thoiGian(LocalDateTime.now())
                    .build();
            nhatKyRepository.save(log);
        } catch (Exception e) {
            // Không để lỗi log làm hỏng flow chính
            log.error("Lỗi khi ghi audit log: {}", e.getMessage());
        }
    }
}
