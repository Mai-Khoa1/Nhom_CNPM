package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity BanToChuc (Ban tổ chức) - mở rộng 1-1 từ tài khoản TaiKhoan có vaiTro = "Ban tổ chức".
 * Mỗi Ban tổ chức là 1 đơn vị tổ chức đua độc lập (multi-tenancy): mỗi mùa giải chỉ thuộc về
 * đúng 1 Ban tổ chức (xem MuaGiai.maBTC), dữ liệu giữa các Ban tổ chức không được lẫn vào nhau.
 */
@Entity
@Table(name = "BanToChuc")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanToChuc {

    public static final String BO_PHAN_DIEU_PHOI = "Điều phối";
    public static final String BO_PHAN_TRONG_TAI = "Trọng tài";
    public static final String BO_PHAN_Y_TE = "Y tế";

    @Id
    @Column(name = "maBTC", length = 50)
    private String maBTC;

    @Column(name = "maTK", nullable = false, unique = true, length = 50)
    private String maTK;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "boPhan", nullable = false, length = 50)
    private String boPhan;

    @Column(name = "chucVu", length = 100)
    private String chucVu;
}
