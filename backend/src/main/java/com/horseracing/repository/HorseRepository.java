package com.horseracing.repository;

import com.horseracing.entity.Horse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Horse entity
 */
@Repository
public interface HorseRepository extends JpaRepository<Horse, Long> {
    List<Horse> findByStatus(String status);
    List<Horse> findByNameContainingIgnoreCase(String name);
}
