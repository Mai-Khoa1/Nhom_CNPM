package com.horseracing.repository;

import com.horseracing.entity.YeuCauCapNhat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity YeuCauCapNhat (yêu cầu duyệt lại thông tin Ngựa/Nài sau khi sửa).
 */
@Repository
public interface YeuCauCapNhatRepository extends JpaRepository<YeuCauCapNhat, String>, JpaSpecificationExecutor<YeuCauCapNhat> {

    /**
     * Các yêu cầu song song khác (gửi tới BTC khác) cùng sửa 1 đối tượng với cùng nội dung mới, còn
     * đang chờ duyệt - dùng để tự động đóng theo nguyên tắc "duyệt đầu tiên thắng" (mục 2.5).
     */
    List<YeuCauCapNhat> findByLoaiDoiTuongAndMaDoiTuongAndDuLieuMoiAndTrangThaiAndMaYeuCauNot(
            String loaiDoiTuong, String maDoiTuong, String duLieuMoi, String trangThai, String maYeuCau);
}
