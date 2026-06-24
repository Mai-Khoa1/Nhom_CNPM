package com.horseracing.repository;

import com.horseracing.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Result (bảng KetQuaThiDua).
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, String> {

    List<Result> findByMaChangDua(String maChangDua);

    List<Result> findByMaNgua(String maNgua);

    Optional<Result> findByMaChangDuaAndMaNgua(String maChangDua, String maNgua);

    List<Result> findByTrangThaiCongBo(String trangThaiCongBo);

    boolean existsByMaChangDuaAndMaNgua(String maChangDua, String maNgua);

    /** Bảng xếp hạng ngựa theo mùa giải: [horseId, horseName, ownerName, totalPoints, totalRaces, totalWins] */
    @Query(value = "SELECT r.maNgua, n.tenNgua, cn.hoTen, SUM(r.diem), COUNT(*), " +
                   "SUM(CASE WHEN r.hang = 1 THEN 1 ELSE 0 END) " +
                   "FROM KetQuaThiDau r " +
                   "JOIN ChangDua cd ON r.maChangDua = cd.maChangDua " +
                   "JOIN Ngua n ON r.maNgua = n.maNgua " +
                   "JOIN ChuNgua cn ON n.maChuNgua = cn.maChuNgua " +
                   "WHERE cd.maMuaGiai = :maMuaGiai " +
                   "GROUP BY r.maNgua, n.tenNgua, cn.hoTen " +
                   "ORDER BY SUM(r.diem) DESC", nativeQuery = true)
    List<Object[]> findHorseRankingBySeason(@Param("maMuaGiai") String maMuaGiai);

    /** Bảng xếp hạng jockey theo mùa giải: [jockeyId, jockeyName, ownerName, totalPoints, totalRaces, totalWins] */
    @Query(value = "SELECT dk.maNaiNgua, j.hoTen, cn.hoTen, SUM(r.diem), COUNT(*), " +
                   "SUM(CASE WHEN r.hang = 1 THEN 1 ELSE 0 END) " +
                   "FROM KetQuaThiDau r " +
                   "JOIN ChangDua cd ON r.maChangDua = cd.maChangDua " +
                   "JOIN DangKyThiDau dk ON dk.maChangDua = r.maChangDua AND dk.maNgua = r.maNgua " +
                   "JOIN Jockey j ON dk.maNaiNgua = j.maNaiNgua " +
                   "JOIN ChuNgua cn ON j.maChuNgua = cn.maChuNgua " +
                   "WHERE cd.maMuaGiai = :maMuaGiai " +
                   "GROUP BY dk.maNaiNgua, j.hoTen, cn.hoTen " +
                   "ORDER BY SUM(r.diem) DESC", nativeQuery = true)
    List<Object[]> findJockeyRankingBySeason(@Param("maMuaGiai") String maMuaGiai);
}
