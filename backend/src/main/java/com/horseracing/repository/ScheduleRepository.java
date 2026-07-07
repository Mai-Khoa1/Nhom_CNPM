package com.horseracing.repository;

import com.horseracing.entity.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho entity Schedule (ChangDua).
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String>, JpaSpecificationExecutor<Schedule> {

    long countByTrangThai(String trangThai);

    long countByMaMuaGiai(String maMuaGiai);

    long countByMaMuaGiaiAndTrangThai(String maMuaGiai, String trangThai);

    /** Tổng số cuộc đua thuộc danh sách mùa giải cho trước - dashboard Ban tổ chức (mục 5.10). */
    long countByMaMuaGiaiIn(java.util.List<String> maMuaGiaiList);

    /** Số cuộc đua ở 1 trạng thái, thuộc danh sách mùa giải cho trước - dashboard Ban tổ chức (mục 5.10). */
    long countByMaMuaGiaiInAndTrangThai(java.util.List<String> maMuaGiaiList, String trangThai);

    /**
     * Đọc kèm khóa ghi (SELECT ... FOR UPDATE) trên đúng 1 dòng ChangDua - dùng để chống race condition
     * giữa việc chuyển trạng thái cuộc đua (publishRace) và việc tạo đăng ký mới (createRegistration)
     * xảy ra đồng thời trên cùng 1 race. Giao dịch nào tới trước giữ khóa tới khi commit/rollback xong,
     * giao dịch sau phải đợi - đảm bảo không có đăng ký "chen ngang" giữa lúc đếm PENDING và lúc update.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.maChangDua = :maChangDua")
    Optional<Schedule> findByIdForUpdate(@Param("maChangDua") String maChangDua);
}
