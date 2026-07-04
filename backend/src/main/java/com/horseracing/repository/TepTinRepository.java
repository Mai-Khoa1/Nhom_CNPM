package com.horseracing.repository;

import com.horseracing.entity.TepTin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TepTinRepository extends JpaRepository<TepTin, String> {

    List<TepTin> findByLoaiFileOrderByNgayTaoDesc(String loaiFile);

    List<TepTin> findByLoaiDoiTuongAndMaDoiTuongOrderByNgayTaoDesc(String loaiDoiTuong, String maDoiTuong);
}
