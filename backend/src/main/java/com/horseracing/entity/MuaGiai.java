package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity MuaGiai (Season) - Ánh xạ bảng MuaGiai trong CSDL.
 */
@Entity
@Table(name = "MuaGiai")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuaGiai {

    public static final String TRANG_THAI_MO_DANG_KY = "Mở đăng ký";
    public static final String TRANG_THAI_DANG_DIEN_RA = "Đang diễn ra";
    public static final String TRANG_THAI_DA_KET_THUC = "Đã kết thúc";
    public static final String TRANG_THAI_DA_HUY = "Đã hủy";

    @Id
    @Column(name = "maMuaGiai", length = 50)
    private String maMuaGiai;

    @Column(name = "tenMuaGiai", nullable = false, length = 100)
    private String tenMuaGiai;

    @Column(name = "ngayBatDau")
    private LocalDate ngayBatDau;

    @Column(name = "ngayKetThuc")
    private LocalDate ngayKetThuc;

    @Column(name = "moTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) {
            trangThai = TRANG_THAI_MO_DANG_KY;
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
