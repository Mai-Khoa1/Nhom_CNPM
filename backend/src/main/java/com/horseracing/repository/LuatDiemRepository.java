package com.horseracing.repository;

import com.horseracing.entity.LuatDiem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LuatDiemRepository extends JpaRepository<LuatDiem, String> {

    List<LuatDiem> findByMaMuaGiaiOrderByHangAsc(String maMuaGiai);

    Optional<LuatDiem> findByMaMuaGiaiAndHang(String maMuaGiai, Integer hang);

    void deleteByMaMuaGiai(String maMuaGiai);
}
