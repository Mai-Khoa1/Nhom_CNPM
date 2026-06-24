package com.horseracing.repository;

import com.horseracing.entity.NaiNgua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity NaiNgua (Jockey)
 */
@Repository
public interface NaiNguaRepository extends JpaRepository<NaiNgua, String> {

    /** Tìm kiếm theo họ tên (không phân biệt hoa thường) */
    List<NaiNgua> findByHoTenContainingIgnoreCase(String hoTen);

    /** Lọc theo trạng thái */
    List<NaiNgua> findByTrangThai(String trangThai);

    /** Lọc theo chủ ngựa */
    List<NaiNgua> findByMaChuNgua(String maChuNgua);

    /** Lọc theo số năm kinh nghiệm tối thiểu */
    List<NaiNgua> findByKinhNghiemGreaterThanEqual(Integer kinhNghiem);

    /** Lọc kết hợp tên + trạng thái + kinh nghiệm tối thiểu */
    List<NaiNgua> findByHoTenContainingIgnoreCaseAndTrangThaiAndKinhNghiemGreaterThanEqual(
            String hoTen, String trangThai, Integer kinhNghiem);

    /** Tìm theo số giấy phép */
    Optional<NaiNgua> findBySoGiayPhep(String soGiayPhep);

    /** Kiểm tra giấy phép đã tồn tại chưa */
    boolean existsBySoGiayPhep(String soGiayPhep);

    /** Kiểm tra giấy phép đã tồn tại ở jockey khác */
    boolean existsBySoGiayPhepAndMaNaiNguaNot(String soGiayPhep, String maNaiNgua);

    /** Kiểm tra jockey có lịch đua sắp tới không */
    @Query(value = "SELECT COUNT(*) > 0 FROM DangKyThiDau dk " +
                   "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
                   "WHERE dk.maNaiNgua = :maNaiNgua " +
                   "  AND cd.trangThai IN ('Chưa diễn ra', 'Đang đua') " +
                   "  AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    boolean hasUpcomingRaces(@Param("maNaiNgua") String maNaiNgua);
}
