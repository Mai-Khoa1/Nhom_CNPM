package com.horseracing.repository;

import com.horseracing.entity.DangKyThiDau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DangKyThiDauRepository extends JpaRepository<DangKyThiDau, String>, JpaSpecificationExecutor<DangKyThiDau> {

    List<DangKyThiDau> findByMaChangDua(String maChangDua);

    /** Tất cả lần đăng ký của 1 ngựa/nài (mọi trạng thái) - dùng để tìm file đính kèm mới nhất làm ảnh đại diện. */
    List<DangKyThiDau> findByMaNgua(String maNgua);

    List<DangKyThiDau> findByMaNaiNgua(String maNaiNgua);

    Page<DangKyThiDau> findByMaChangDua(String maChangDua, Pageable pageable);

    /** Đếm số đăng ký "còn chiếm slot" của 1 race - loại trừ các trạng thái đã giải phóng slot (Từ chối/Đã hủy). */
    long countByMaChangDuaAndTrangThaiNotIn(String maChangDua, List<String> excludedTrangThai);

    long countByTrangThai(String trangThai);

    /** Đếm số đăng ký ĐÃ DUYỆT của 1 race - dùng để kiểm tra đủ số lượng tối thiểu trước khi cho chuyển ONGOING. */
    long countByMaChangDuaAndTrangThai(String maChangDua, String trangThai);

    /** Danh sách đăng ký ĐÃ DUYỆT của 1 race - dùng để kiểm tra đã phân làn đầy đủ trước khi chuyển ONGOING. */
    List<DangKyThiDau> findByMaChangDuaAndTrangThai(String maChangDua, String trangThai);

    /** Chặn đăng ký trùng ngựa/nài cho cùng 1 race CHỈ khi bản ghi cũ đang active (không tính Từ chối/Đã hủy - có thể đăng ký lại). */
    boolean existsByMaChangDuaAndMaNguaAndTrangThaiIn(String maChangDua, String maNgua, List<String> activeTrangThai);

    boolean existsByMaChangDuaAndMaNaiNguaAndTrangThaiIn(String maChangDua, String maNaiNgua, List<String> activeTrangThai);

    boolean existsByMaChangDuaAndSoLanAndMaDangKyNot(String maChangDua, Integer soLan, String maDangKy);

    /**
     * Danh sách maBTC (Ban tổ chức) đang có ít nhất 1 đăng ký APPROVED tham chiếu ngựa này - dùng để
     * xác định cần gửi yêu cầu cập nhật thông tin ngựa tới những Ban tổ chức nào (mục 2.5 - 1 ngựa có
     * thể đang thi đấu chính thức ở nhiều Ban tổ chức cùng lúc).
     */
    @Query(value = "SELECT DISTINCT mg.maBTC FROM DangKyThiDau dk " +
            "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
            "JOIN MuaGiai mg ON cd.maMuaGiai = mg.maMuaGiai " +
            "WHERE dk.maNgua = :maNgua AND dk.trangThai = 'Đã duyệt' AND mg.maBTC IS NOT NULL",
            nativeQuery = true)
    List<String> findDistinctOrganizerIdsByApprovedHorse(@Param("maNgua") String maNgua);

    /** Tương tự findDistinctOrganizerIdsByApprovedHorse nhưng theo nài ngựa. */
    @Query(value = "SELECT DISTINCT mg.maBTC FROM DangKyThiDau dk " +
            "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
            "JOIN MuaGiai mg ON cd.maMuaGiai = mg.maMuaGiai " +
            "WHERE dk.maNaiNgua = :maNaiNgua AND dk.trangThai = 'Đã duyệt' AND mg.maBTC IS NOT NULL",
            nativeQuery = true)
    List<String> findDistinctOrganizerIdsByApprovedJockey(@Param("maNaiNgua") String maNaiNgua);

    /** Danh sách maNgua có ít nhất 1 đăng ký APPROVED - dùng để lọc ngựa hiển thị cho khán giả (mục 4.2). */
    @Query("SELECT DISTINCT dk.maNgua FROM DangKyThiDau dk WHERE dk.trangThai = 'Đã duyệt'")
    List<String> findDistinctApprovedHorseIds();

    /** Danh sách maNaiNgua có ít nhất 1 đăng ký APPROVED - dùng để lọc nài hiển thị cho khán giả (mục 4.2). */
    @Query("SELECT DISTINCT dk.maNaiNgua FROM DangKyThiDau dk WHERE dk.trangThai = 'Đã duyệt'")
    List<String> findDistinctApprovedJockeyIds();

    /** Số ngựa (không trùng) có ít nhất 1 đăng ký APPROVED trên toàn hệ thống - dashboard khán giả. */
    @Query("SELECT COUNT(DISTINCT dk.maNgua) FROM DangKyThiDau dk WHERE dk.trangThai = 'Đã duyệt'")
    long countDistinctApprovedHorses();

    /** Số nài (không trùng) có ít nhất 1 đăng ký APPROVED trên toàn hệ thống - dashboard khán giả. */
    @Query("SELECT COUNT(DISTINCT dk.maNaiNgua) FROM DangKyThiDau dk WHERE dk.trangThai = 'Đã duyệt'")
    long countDistinctApprovedJockeys();

    /** Số ngựa (không trùng) có đăng ký APPROVED tại đúng 1 Ban tổ chức - dashboard Ban tổ chức (mục 5.10). */
    @Query(value = "SELECT COUNT(DISTINCT dk.maNgua) FROM DangKyThiDau dk " +
            "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
            "JOIN MuaGiai mg ON cd.maMuaGiai = mg.maMuaGiai " +
            "WHERE mg.maBTC = :maBTC AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    long countDistinctApprovedHorsesByOrganizer(@Param("maBTC") String maBTC);

    /** Số nài (không trùng) có đăng ký APPROVED tại đúng 1 Ban tổ chức - dashboard Ban tổ chức (mục 5.10). */
    @Query(value = "SELECT COUNT(DISTINCT dk.maNaiNgua) FROM DangKyThiDau dk " +
            "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
            "JOIN MuaGiai mg ON cd.maMuaGiai = mg.maMuaGiai " +
            "WHERE mg.maBTC = :maBTC AND dk.trangThai = 'Đã duyệt'", nativeQuery = true)
    long countDistinctApprovedJockeysByOrganizer(@Param("maBTC") String maBTC);

    /** Số đăng ký Chờ duyệt thuộc 1 Ban tổ chức - dashboard Ban tổ chức. */
    @Query(value = "SELECT COUNT(*) FROM DangKyThiDau dk " +
            "JOIN ChangDua cd ON dk.maChangDua = cd.maChangDua " +
            "JOIN MuaGiai mg ON cd.maMuaGiai = mg.maMuaGiai " +
            "WHERE mg.maBTC = :maBTC AND dk.trangThai = 'Chờ duyệt'", nativeQuery = true)
    long countPendingByOrganizer(@Param("maBTC") String maBTC);
}
