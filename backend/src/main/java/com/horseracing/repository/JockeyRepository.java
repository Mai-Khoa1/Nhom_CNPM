package com.horseracing.repository;

import com.horseracing.entity.Jockey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Jockey entity
 */
@Repository
public interface JockeyRepository extends JpaRepository<Jockey, Long> {
    List<Jockey> findByNameContainingIgnoreCase(String name);
}
