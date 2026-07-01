package com.horseracing.repository;

import com.horseracing.entity.NaiNgua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity NaiNgua (Jockey/nài ngựa).
 */
@Repository
public interface NaiNguaRepository extends JpaRepository<NaiNgua, String>, JpaSpecificationExecutor<NaiNgua> {

    Page<NaiNgua> findByMaChuNgua(String maChuNgua, Pageable pageable);

    java.util.List<NaiNgua> findByMaChuNgua(String maChuNgua);

    long countByTrangThai(String trangThai);

    boolean existsBySoGiayPhep(String soGiayPhep);

    boolean existsBySoGiayPhepAndMaNaiNguaNot(String soGiayPhep, String maNaiNgua);

    /** Đếm số chặng đua chưa diễn ra/đang đua mà jockey đã được duyệt đăng ký tham gia */
    @Query(value = "SELECT COUNT(*) FROM DangKyThiDau dk " +
                   "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
                   "WHERE dk.maNaiNgua = :maNaiNgua " +
                   "  AND cd.trangThai IN ('Mở đăng ký', 'Đã đóng đăng ký', 'Đang đua') " +
                   "  AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    long countUpcomingRaces(@Param("maNaiNgua") String maNaiNgua);
}
