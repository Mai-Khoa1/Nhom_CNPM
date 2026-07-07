package com.horseracing.repository;

import com.horseracing.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Result (KetQuaThiDau).
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, String> {

    List<Result> findByMaChangDua(String maChangDua);

    Optional<Result> findByMaChangDuaAndMaNgua(String maChangDua, String maNgua);

    List<Result> findByMaNgua(String maNgua);

    /** Bảng xếp hạng ngựa theo mùa giải: maNgua, tenNgua, tenChuNgua, tongDiem, tongSoChang, tongSoThang */
    @Query(value = "SELECT n.maNgua, n.tenNgua, c.hoTen, COALESCE(SUM(kq.diem), 0), COUNT(*), " +
            "SUM(CASE WHEN kq.hang = 1 THEN 1 ELSE 0 END) " +
            "FROM KetQuaThiDau kq " +
            "JOIN ChangDua cd ON kq.maChangDua = cd.maChangDua " +
            "JOIN DangKyThiDau dk ON dk.maChangDua = kq.maChangDua AND dk.maNgua = kq.maNgua " +
            "JOIN Ngua n ON kq.maNgua = n.maNgua " +
            "JOIN ChuNgua c ON n.maChuNgua = c.maChuNgua " +
            "WHERE cd.maMuaGiai = :maMuaGiai AND kq.trangThaiCongBo = 'Đã công bố' AND dk.trangThai = 'Đã duyệt' " +
            "GROUP BY n.maNgua, n.tenNgua, c.hoTen " +
            "ORDER BY SUM(kq.diem) DESC", nativeQuery = true)
    List<Object[]> findHorseRankingBySeason(@Param("maMuaGiai") String maMuaGiai);

    /** Bảng xếp hạng jockey theo mùa giải: maNaiNgua, hoTen, tenChuNgua, tongDiem, tongSoChang, tongSoThang */
    @Query(value = "SELECT j.maNaiNgua, j.hoTen, c.hoTen, COALESCE(SUM(kq.diem), 0), COUNT(*), " +
            "SUM(CASE WHEN kq.hang = 1 THEN 1 ELSE 0 END) " +
            "FROM KetQuaThiDau kq " +
            "JOIN ChangDua cd ON kq.maChangDua = cd.maChangDua " +
            "JOIN DangKyThiDau dk ON dk.maChangDua = kq.maChangDua AND dk.maNgua = kq.maNgua " +
            "JOIN Jockey j ON dk.maNaiNgua = j.maNaiNgua " +
            "JOIN ChuNgua c ON j.maChuNgua = c.maChuNgua " +
            "WHERE cd.maMuaGiai = :maMuaGiai AND kq.trangThaiCongBo = 'Đã công bố' AND dk.trangThai = 'Đã duyệt' " +
            "GROUP BY j.maNaiNgua, j.hoTen, c.hoTen " +
            "ORDER BY SUM(kq.diem) DESC", nativeQuery = true)
    List<Object[]> findJockeyRankingBySeason(@Param("maMuaGiai") String maMuaGiai);
}
