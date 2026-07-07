package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity TepTin (File) - lưu thông tin file đã upload (ảnh ngựa/jockey, hồ sơ sức khỏe...).
 */
@Entity
@Table(name = "TepTin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TepTin {

    @Id
    @Column(name = "maTepTin", length = 50)
    private String maTepTin;

    @Column(name = "tenFile", nullable = false, length = 255)
    private String tenFile;

    @Column(name = "duongDan", nullable = false, length = 500)
    private String duongDan;

    @Column(name = "loaiFile", length = 50)
    private String loaiFile;

    /** Content-Type gốc của file (image/jpeg, application/pdf...) - dùng để trả đúng header khi phục vụ tải/xem. */
    @Column(name = "contentType", length = 100)
    private String contentType;

    /** "DANG_KY" - tệp tin gắn với 1 lần đăng ký thi đấu cụ thể (xem DangKyThiDau), không gắn thẳng vào Ngựa/Nài nữa. */
    @Column(name = "loaiDoiTuong", length = 50)
    private String loaiDoiTuong;

    @Column(name = "maDoiTuong", length = 50)
    private String maDoiTuong;

    @Column(name = "kichThuoc")
    private Long kichThuoc;

    @Column(name = "maTK", length = 50)
    private String maTK;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
