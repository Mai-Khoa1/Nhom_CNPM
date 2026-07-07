package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity NaiNgua (Jockey/nài ngựa) - Ánh xạ bảng Jockey trong CSDL.
 */
@Entity
@Table(name = "Jockey")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaiNgua {

    public static final String TRANG_THAI_HOAT_DONG = "Hoạt động";
    public static final String TRANG_THAI_NGUNG_HOAT_DONG = "Ngừng hoạt động";

    @Id
    @Column(name = "maNaiNgua", length = 50)
    private String maNaiNgua;

    @Column(name = "maChuNgua", nullable = false, length = 50)
    private String maChuNgua;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngaySinh")
    private LocalDate ngaySinh;

    @Column(name = "gioiTinh")
    @Enumerated(EnumType.STRING)
    private Ngua.GioiTinh gioiTinh;

    @Column(name = "quocTich", length = 50)
    private String quocTich;

    @Column(name = "kinhNghiem")
    private Integer kinhNghiem;

    @Column(name = "soGiayPhep", unique = true, length = 50)
    private String soGiayPhep;

    @Column(name = "canNang")
    private Double canNang;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "tyLeThang")
    private Double tyLeThang;

    @Column(name = "ghiChu", length = 500)
    private String ghiChu;

    /** Xóa mềm (lỗi 6): "Ngừng hoạt động" khi hồ sơ đã từng có đăng ký thi đấu và bị người dùng "xóa". */
    @Column(name = "trangThai", length = 30)
    private String trangThai;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngayCapNhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TRANG_THAI_HOAT_DONG;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
