package com.horseracing.repository;

import com.horseracing.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, String> {

    Page<ThongBao> findByMaTK(String maTK, Pageable pageable);

    Page<ThongBao> findByMaTKAndTrangThai(String maTK, String trangThai, Pageable pageable);

    List<ThongBao> findByMaTKAndTrangThai(String maTK, String trangThai);

    long countByMaTKAndTrangThai(String maTK, String trangThai);
}
