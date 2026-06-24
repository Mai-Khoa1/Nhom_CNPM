package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity User - Ánh xạ bảng TaiKhoan trong CSDL.
 * vaiTro / trangThai lưu trực tiếp giá trị tiếng Việt của ENUM trong DB
 * (không dùng Java enum vì các giá trị có khoảng trắng, ví dụ "Người xem").
 * Việc map sang Role/UserStatus tiếng Anh cho tầng API nằm ở DTO layer.
 */
@Entity
@Table(name = "TaiKhoan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    public static final String VAI_TRO_ADMIN = "Admin";
    public static final String VAI_TRO_BAN_TO_CHUC = "Ban tổ chức";
    public static final String VAI_TRO_CHU_NGUA = "Chủ ngựa";
    public static final String VAI_TRO_NGUOI_XEM = "Người xem";

    public static final String TRANG_THAI_HOAT_DONG = "Hoạt động";
    public static final String TRANG_THAI_BI_KHOA = "Bị khóa";

    @Id
    @Column(name = "maTK", length = 50)
    private String maTK;

    @Column(name = "tenDangNhap", nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(name = "matKhau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "soDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "vaiTro", nullable = false, length = 50)
    private String vaiTro;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = TRANG_THAI_HOAT_DONG;
        }
    }
}
