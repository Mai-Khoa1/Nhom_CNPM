package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity Ngua - Ánh xạ bảng Ngua trong CSDL
 * Thay thế entity Horse cũ để đồng bộ với schema.sql
 */
@Entity
@Table(name = "Ngua")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ngua {

    @Id
    @Column(name = "maNgua", length = 50)
    private String maNgua;

    @Column(name = "maChuNgua", nullable = false, length = 50)
    private String maChuNgua;

    @Column(name = "tenNgua", nullable = false, length = 100)
    private String tenNgua;

    @Column(name = "giongNgua", length = 50)
    private String giongNgua;

    @Column(name = "ngaySinh")
    private LocalDate ngaySinh;

    @Column(name = "gioiTinh")
    @Enumerated(EnumType.STRING)
    private GioiTinh gioiTinh;

    @Column(name = "mauLong", length = 30)
    private String mauLong;

    @Column(name = "troiLuong")
    private Double troiLuong;

    @Column(name = "trangThaiSucKhoe", length = 255)
    private String trangThaiSucKhoe;

    @Column(name = "trangThai", length = 50)
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
            trangThai = "Chờ duyệt";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    public enum GioiTinh {
        Đực, Cái
    }
}
