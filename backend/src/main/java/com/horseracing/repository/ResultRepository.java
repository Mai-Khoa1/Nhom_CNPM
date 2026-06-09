package com.horseracing.repository;

import com.horseracing.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Result entity
 */
@Repository
public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByRaceNameContainingIgnoreCase(String raceName);
}
