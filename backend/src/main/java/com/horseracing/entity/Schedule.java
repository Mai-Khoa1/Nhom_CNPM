package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity Schedule (lịch đua / chặng đua) - Ánh xạ bảng ChangDua trong CSDL.
 * Tên class giữ "Schedule" theo yêu cầu hệ thống, bảng vật lý là ChangDua.
 */
@Entity
@Table(name = "ChangDua")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    public static final String TRANG_THAI_MO_DANG_KY = "Mở đăng ký";
    public static final String TRANG_THAI_DA_DONG_DANG_KY = "Đã đóng đăng ký";
    public static final String TRANG_THAI_DANG_DUA = "Đang đua";
    public static final String TRANG_THAI_HOAN_THANH = "Hoàn thành";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    /** Mùa giải mặc định dùng khi không chỉ định maMuaGiai. */
    public static final String MA_MUA_GIAI_DEFAULT = "MG_DEFAULT";

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

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "moTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (maMuaGiai == null || maMuaGiai.isBlank()) {
            maMuaGiai = MA_MUA_GIAI_DEFAULT;
        }
        if (soLanDua == null) {
            soLanDua = 1;
        }
        if (trangThai == null) {
            trangThai = TRANG_THAI_MO_DANG_KY;
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
