package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity NaiNgua (Jockey) - Ánh xạ bảng Jockey trong CSDL
 * Thay thế entity Jockey cũ để đồng bộ với schema.sql
 */
@Entity
@Table(name = "Jockey")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NaiNgua {

    @Id
    @Column(name = "maNaiNgua", length = 50)
    private String maNaiNgua;

    @Column(name = "maChuNgua", nullable = false, length = 50)
    private String maChuNgua;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "ngaySinh")
    private LocalDate ngaySinh;

    @Column(name = "quocTich", length = 50)
    private String quocTich;

    @Column(name = "kinhNghiem")
    private Integer kinhNghiem;

    @Column(name = "soGiayPhep", unique = true, length = 50)
    private String soGiayPhep;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    // Các trường chỉ số sức khỏe (cập nhật bởi updateStats)
    @Column(name = "canNang")
    private Double canNang;

    @Column(name = "bmi")
    private Double bmi;

    @Column(name = "tyLeThang")
    private Double tyLeThang;

    @Column(name = "ghiChu", length = 500)
    private String ghiChu;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngayCapNhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = "Sẵn sàng";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
