package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity ThongBao (Notification) - thông báo hệ thống gửi tới người dùng.
 */
@Entity
@Table(name = "ThongBao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBao {

    public static final String CHUA_DOC = "Chưa đọc";
    public static final String DA_DOC = "Đã đọc";

    @Id
    @Column(name = "maThongBao", length = 50)
    private String maThongBao;

    @Column(name = "maTK", nullable = false, length = 50)
    private String maTK;

    @Column(name = "tieuDe", nullable = false, length = 150)
    private String tieuDe;

    @Column(name = "noiDung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "loai", length = 50)
    private String loai;

    @Column(name = "loaiDoiTuong", length = 50)
    private String loaiDoiTuong;

    @Column(name = "maDoiTuong", length = 50)
    private String maDoiTuong;

    @Column(name = "ngayGui", updatable = false)
    private LocalDateTime ngayGui;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @PrePersist
    protected void onCreate() {
        if (ngayGui == null) {
            ngayGui = LocalDateTime.now();
        }
        if (trangThai == null) {
            trangThai = CHUA_DOC;
        }
    }
}
