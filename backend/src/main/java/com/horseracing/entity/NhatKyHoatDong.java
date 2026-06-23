package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity NhatKyHoatDong - Audit log cho các thao tác quan trọng
 * Dùng cho JockeyService.writeAuditLog() theo sơ đồ tuần tự
 */
@Entity
@Table(name = "NhatKyHoatDong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKyHoatDong {

    @Id
    @Column(name = "maNhatKy", length = 50)
    private String maNhatKy;

    @Column(name = "maTK", length = 50)
    private String maTK;

    @Column(name = "loaiHanhDong", length = 50)
    private String loaiHanhDong;

    @Column(name = "moTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "thoiGian")
    private LocalDateTime thoiGian;

    @Column(name = "diaChiIP", length = 45)
    private String diaChiIP;

    @Column(name = "doiTuongTacDong", length = 100)
    private String doiTuongTacDong;

    @PrePersist
    protected void onCreate() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
}
