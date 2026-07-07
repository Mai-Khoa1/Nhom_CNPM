package com.horseracing.repository;

import com.horseracing.entity.Ngua;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Ngua.
 */
@Repository
public interface NguaRepository extends JpaRepository<Ngua, String>, JpaSpecificationExecutor<Ngua> {

    Page<Ngua> findByMaChuNgua(String maChuNgua, Pageable pageable);

    List<Ngua> findByMaChuNgua(String maChuNgua);
}
