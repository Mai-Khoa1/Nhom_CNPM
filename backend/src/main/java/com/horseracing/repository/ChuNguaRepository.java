package com.horseracing.repository;

import com.horseracing.entity.ChuNgua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity ChuNgua.
 */
@Repository
public interface ChuNguaRepository extends JpaRepository<ChuNgua, String> {

    Optional<ChuNgua> findByMaTK(String maTK);
}
