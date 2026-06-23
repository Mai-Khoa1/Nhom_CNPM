package com.horseracing.repository;

import com.horseracing.entity.NhatKyHoatDong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity NhatKyHoatDong (Audit Log).
 */
@Repository
public interface NhatKyHoatDongRepository extends JpaRepository<NhatKyHoatDong, String> {

    List<NhatKyHoatDong> findByMaTKOrderByThoiGianDesc(String maTK);

    List<NhatKyHoatDong> findByLoaiHanhDongOrderByThoiGianDesc(String loaiHanhDong);

    List<NhatKyHoatDong> findByDoiTuongTacDongContainingIgnoreCaseOrderByThoiGianDesc(String doiTuong);

    List<NhatKyHoatDong> findAllByOrderByThoiGianDesc();
}
