package com.horseracing.repository;

import com.horseracing.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity Schedule (bảng ChangDua).
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String>, JpaSpecificationExecutor<Schedule> {

    long countByTrangThai(String trangThai);
}
