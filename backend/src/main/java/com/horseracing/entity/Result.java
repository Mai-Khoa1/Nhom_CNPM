package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity Result (KetQuaThiDau) - Ánh xạ bảng KetQuaThiDau trong CSDL.
 */
@Entity
@Table(name = "KetQuaThiDau")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result {

    public static final String TRANG_THAI_CHUA_CONG_BO = "Chưa công bố";
    public static final String TRANG_THAI_DA_CONG_BO = "Đã công bố";

    @Id
    @Column(name = "maKetQua", length = 50)
    private String maKetQua;

    @Column(name = "maChangDua", nullable = false, length = 50)
    private String maChangDua;

    @Column(name = "maNgua", nullable = false, length = 50)
    private String maNgua;

    @Column(name = "hang", nullable = false)
    private Integer hang;

    @Column(name = "thoiGianHoanThanh", length = 50)
    private String thoiGianHoanThanh;

    @Column(name = "diem")
    private Double diem;

    @Column(name = "ghiChuChuyenMon", columnDefinition = "TEXT")
    private String ghiChuChuyenMon;

    @Column(name = "trangThaiCongBo", length = 50)
    private String trangThaiCongBo;

    @Column(name = "ngayCongBo")
    private LocalDateTime ngayCongBo;

    @PrePersist
    protected void onCreate() {
        if (trangThaiCongBo == null) {
            trangThaiCongBo = TRANG_THAI_CHUA_CONG_BO;
        }
    }
}
