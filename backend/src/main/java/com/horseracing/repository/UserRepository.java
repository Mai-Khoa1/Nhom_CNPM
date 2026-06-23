package com.horseracing.repository;

import com.horseracing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity User (bảng TaiKhoan).
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByTenDangNhap(String tenDangNhap);

    Optional<User> findByEmail(String email);

    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsByEmail(String email);

    List<User> findByVaiTroIn(List<String> vaiTroList);
}
