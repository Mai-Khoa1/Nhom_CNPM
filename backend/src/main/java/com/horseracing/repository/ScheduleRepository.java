package com.horseracing.repository;

import com.horseracing.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity Schedule (ChangDua).
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String>, JpaSpecificationExecutor<Schedule> {

    long countByTrangThai(String trangThai);

    long countByMaMuaGiai(String maMuaGiai);

    long countByMaMuaGiaiAndTrangThai(String maMuaGiai, String trangThai);
}
