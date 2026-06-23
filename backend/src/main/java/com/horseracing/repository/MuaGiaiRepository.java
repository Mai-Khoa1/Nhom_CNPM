package com.horseracing.repository;

import com.horseracing.entity.MuaGiai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MuaGiaiRepository extends JpaRepository<MuaGiai, String>, JpaSpecificationExecutor<MuaGiai> {
}
