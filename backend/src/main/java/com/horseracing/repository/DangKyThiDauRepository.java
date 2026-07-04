package com.horseracing.repository;

import com.horseracing.entity.DangKyThiDau;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DangKyThiDauRepository extends JpaRepository<DangKyThiDau, String>, JpaSpecificationExecutor<DangKyThiDau> {

    List<DangKyThiDau> findByMaChangDua(String maChangDua);

    Page<DangKyThiDau> findByMaChangDua(String maChangDua, Pageable pageable);

    long countByMaChangDua(String maChangDua);

    long countByMaChangDuaAndTrangThaiNot(String maChangDua, String trangThai);

    long countByTrangThai(String trangThai);

    boolean existsByMaChangDuaAndMaNgua(String maChangDua, String maNgua);

    boolean existsByMaChangDuaAndMaNaiNgua(String maChangDua, String maNaiNgua);

    boolean existsByMaChangDuaAndSoLanAndMaDangKyNot(String maChangDua, Integer soLan, String maDangKy);
}
