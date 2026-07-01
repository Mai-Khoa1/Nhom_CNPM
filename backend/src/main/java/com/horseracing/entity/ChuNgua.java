package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity ChuNgua - Ánh xạ bảng ChuNgua trong CSDL (thông tin mở rộng của chủ ngựa, 1-1 với TaiKhoan).
 */
@Entity
@Table(name = "ChuNgua")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuNgua {

    @Id
    @Column(name = "maChuNgua", length = 50)
    private String maChuNgua;

    @Column(name = "maTK", nullable = false, unique = true, length = 50)
    private String maTK;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "soDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    private String email;
}
