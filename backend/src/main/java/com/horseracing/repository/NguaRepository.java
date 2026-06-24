package com.horseracing.repository;

import com.horseracing.entity.Ngua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Ngua
 */
@Repository
public interface NguaRepository extends JpaRepository<Ngua, String> {

    /** Tìm kiếm theo tên (không phân biệt hoa thường) */
    List<Ngua> findByTenNguaContainingIgnoreCase(String tenNgua);

    /** Lọc theo trạng thái */
    List<Ngua> findByTrangThai(String trangThai);

    /** Lọc theo chủ ngựa */
    List<Ngua> findByMaChuNgua(String maChuNgua);

    /** Lọc theo tên và trạng thái */
    List<Ngua> findByTenNguaContainingIgnoreCaseAndTrangThai(String tenNgua, String trangThai);

    /** Kiểm tra ngựa có trong lịch đua (ChangDua chưa diễn ra hoặc đang đua) không */
    @Query(value = "SELECT COUNT(*) > 0 FROM DangKyThiDau dk " +
                   "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
                   "WHERE dk.maNgua = :maNgua " +
                   "  AND cd.trangThai IN ('Chưa diễn ra', 'Đang đua') " +
                   "  AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    boolean hasUpcomingRaces(@Param("maNgua") String maNgua);

    /** Kiểm tra mã ngựa đã tồn tại chưa */
    boolean existsByMaNgua(String maNgua);
}
