package com.horseracing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity YeuCauCapNhat (UpdateRequest) - yêu cầu chủ ngựa sửa thông tin Ngựa/Nài đã được duyệt
 * trước đó, chờ Ban tổ chức duyệt lại. Dữ liệu gốc trong Ngua/Jockey chỉ bị ghi đè khi DUYỆT.
 */
@Entity
@Table(name = "YeuCauCapNhat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YeuCauCapNhat {

    public static final String LOAI_NGUA = "NGUA";
    public static final String LOAI_NAI_NGUA = "NAI_NGUA";
    public static final String LOAI_TEP_TIN = "TEP_TIN";

    public static final String TRANG_THAI_CHO_DUYET = "Chờ duyệt";
    public static final String TRANG_THAI_DA_DUYET = "Đã duyệt";
    public static final String TRANG_THAI_TU_CHOI = "Từ chối";

    /** hanhDong: loại thao tác yêu cầu áp dụng khi được duyệt - CAP_NHAT (sửa) hoặc XOA (xóa). */
    public static final String HANH_DONG_CAP_NHAT = "CAP_NHAT";
    public static final String HANH_DONG_XOA = "XOA";

    @Id
    @Column(name = "maYeuCau", length = 50)
    private String maYeuCau;

    @Column(name = "loaiDoiTuong", nullable = false, length = 20)
    private String loaiDoiTuong;

    @Column(name = "maDoiTuong", nullable = false, length = 50)
    private String maDoiTuong;

    @Column(name = "maTK", nullable = false, length = 50)
    private String maTK;

    /** Ban tổ chức cần duyệt yêu cầu này (null với yêu cầu tệp tin ở giai đoạn hiện tại). */
    @Column(name = "maBTC", length = 50)
    private String maBTC;

    @Column(name = "duLieuCu", nullable = false, columnDefinition = "TEXT")
    private String duLieuCu;

    @Column(name = "duLieuMoi", nullable = false, columnDefinition = "TEXT")
    private String duLieuMoi;

    @Column(name = "trangThai", length = 50)
    private String trangThai;

    @Column(name = "hanhDong", length = 20)
    private String hanhDong;

    @Column(name = "lyDoTuChoi", length = 255)
    private String lyDoTuChoi;

    @Column(name = "ngayTao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngayXuLy")
    private LocalDateTime ngayXuLy;

    @PrePersist
    protected void onCreate() {
        if (trangThai == null) {
            trangThai = TRANG_THAI_CHO_DUYET;
        }
        if (hanhDong == null) {
            hanhDong = HANH_DONG_CAP_NHAT;
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
