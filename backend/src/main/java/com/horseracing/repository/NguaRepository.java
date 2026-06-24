package com.horseracing.repository;

import com.horseracing.entity.Ngua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Ngua.
 */
@Repository
public interface NguaRepository extends JpaRepository<Ngua, String>, JpaSpecificationExecutor<Ngua> {

    Page<Ngua> findByMaChuNgua(String maChuNgua, Pageable pageable);

    List<Ngua> findByMaChuNgua(String maChuNgua);

    long countByTrangThai(String trangThai);

    /** Kiểm tra ngựa có đang nằm trong chặng đua chưa diễn ra/đang đua và đã được duyệt đăng ký không */
    @Query(value = "SELECT COUNT(*) > 0 FROM DangKyThiDau dk " +
                   "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
                   "WHERE dk.maNgua = :maNgua " +
                   "  AND cd.trangThai IN ('Mở đăng ký', 'Đã đóng đăng ký', 'Đang đua') " +
                   "  AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    boolean hasUpcomingRaces(@Param("maNgua") String maNgua);
}
