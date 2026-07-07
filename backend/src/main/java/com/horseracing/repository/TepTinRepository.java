package com.horseracing.repository;

import com.horseracing.entity.TepTin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TepTinRepository extends JpaRepository<TepTin, String>, JpaSpecificationExecutor<TepTin> {

    List<TepTin> findByLoaiFileOrderByNgayTaoDesc(String loaiFile);

    List<TepTin> findByLoaiDoiTuongAndMaDoiTuongOrderByNgayTaoDesc(String loaiDoiTuong, String maDoiTuong);

    /** Tìm file mới nhất theo loại, trong số các đăng ký (maDoiTuong) thuộc về 1 ngựa/nài cụ thể - dùng để lấy ảnh đại diện. */
    List<TepTin> findByLoaiDoiTuongAndMaDoiTuongInOrderByNgayTaoDesc(String loaiDoiTuong, List<String> maDoiTuongs);
}
