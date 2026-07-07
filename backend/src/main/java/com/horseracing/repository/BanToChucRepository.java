package com.horseracing.repository;

import com.horseracing.entity.BanToChuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity BanToChuc.
 */
@Repository
public interface BanToChucRepository extends JpaRepository<BanToChuc, String> {

    Optional<BanToChuc> findByMaTK(String maTK);
}
