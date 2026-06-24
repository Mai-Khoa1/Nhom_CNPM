package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity DangKyThiDau (Registration) - đăng ký ngựa + jockey thi đấu trong một chặng đua.
 * Cột soLan/ngayGanLan dùng cho việc gán làn đua (Lane).
 */
@Entity
@Table(name = "DangKyThiDau")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangKyThiDau {

    public static final String TRANG_THAI_CHO_DUYET = "Chờ duyệt";
    public static final String TRANG_THAI_DA_DUYET = "Đã duyệt";
    public static final String TRANG_THAI_TU_CHOI = "Từ chối";

    @Id
    @Column(name = "maDangKy", length = 50)
    private String maDangKy;

    @Column(name = "maChangDua", nullable = false, length = 50)
    private String maChangDua;

    @Column(name = "maNgua", nullable = false, length = 50)
    private String maNgua;

    @Column(name = "maNaiNgua", nullable = false, length = 50)
    private String maNaiNgua;

    @Column(name = "ngayDangKy", updatable = false)
    private LocalDateTime ngayDangKy;

    @Column(name = "lanChay")
    private Integer lanChay;

    @Column(name = "soLan")
    private Integer soLan;

    @Column(name = "ngayGanLan")
    private LocalDateTime ngayGanLan;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "lyDoTuChoi", length = 255)
    private String lyDoTuChoi;

    @Column(name = "ghiChu", length = 255)
    private String ghiChu;

    @PrePersist
    protected void onCreate() {
        if (ngayDangKy == null) {
            ngayDangKy = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = TRANG_THAI_CHO_DUYET;
        }
    }
}
