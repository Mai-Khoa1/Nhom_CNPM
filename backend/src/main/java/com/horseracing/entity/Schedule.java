package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity Schedule (ChangDua/Race) - Ánh xạ bảng ChangDua trong CSDL.
 */
@Entity
@Table(name = "ChangDua")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    /** Mã mùa giải mặc định, dùng khi tạo chặng đua không chỉ định mùa giải cụ thể. */
    public static final String MA_MUA_GIAI_DEFAULT = "MG_DEFAULT";

    public static final String TRANG_THAI_MO_DANG_KY = "Mở đăng ký";
    public static final String TRANG_THAI_DA_DONG_DANG_KY = "Đã đóng đăng ký";
    public static final String TRANG_THAI_DANG_DUA = "Đang đua";
    public static final String TRANG_THAI_HOAN_THANH = "Hoàn thành";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    @Id
    @Column(name = "maChangDua", length = 50)
    private String maChangDua;

    @Column(name = "maMuaGiai", nullable = false, length = 50)
    private String maMuaGiai;

    @Column(name = "tenChangDua", nullable = false, length = 100)
    private String tenChangDua;

    @Column(name = "ngayThiDau")
    private LocalDate ngayThiDau;

    @Column(name = "gioBatDau")
    private LocalTime gioBatDau;

    @Column(name = "diaDiem", length = 255)
    private String diaDiem;

    @Column(name = "cuLy")
    private Integer cuLy;

    @Column(name = "loaiMatSan", length = 50)
    private String loaiMatSan;

    @Column(name = "soLanDua")
    private Integer soLanDua;

    @Column(name = "soNguaToiDa")
    private Integer soNguaToiDa;

    @Column(name = "soLuongToiThieu")
    private Integer soLuongToiThieu;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "moTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    /** Số đăng ký ĐÃ DUYỆT tối thiểu mặc định để cho phép chuyển OPEN -> ONGOING nếu không chỉ định. */
    public static final int DEFAULT_SO_LUONG_TOI_THIEU = 2;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) {
            trangThai = TRANG_THAI_MO_DANG_KY;
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (soLuongToiThieu == null) {
            soLuongToiThieu = DEFAULT_SO_LUONG_TOI_THIEU;
        }
    }
}
