package com.horseracing.repository;

import com.horseracing.entity.YeuCauCapNhat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository cho entity YeuCauCapNhat (yêu cầu duyệt lại thông tin Ngựa/Nài sau khi sửa).
 */
@Repository
public interface YeuCauCapNhatRepository extends JpaRepository<YeuCauCapNhat, String>, JpaSpecificationExecutor<YeuCauCapNhat> {
}
