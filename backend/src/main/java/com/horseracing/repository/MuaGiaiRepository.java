package com.horseracing.repository;

import com.horseracing.entity.MuaGiai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MuaGiaiRepository extends JpaRepository<MuaGiai, String>, JpaSpecificationExecutor<MuaGiai> {

    /** Số mùa giải thuộc 1 Ban tổ chức - dashboard Ban tổ chức (mục 5.10). */
    long countByMaBTC(String maBTC);
}
