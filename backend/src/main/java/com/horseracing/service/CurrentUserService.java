package com.horseracing.service;

import com.horseracing.entity.User;
import com.horseracing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Tiện ích lấy maTK (mã tài khoản) của người dùng đang đăng nhập từ Authentication,
 * dùng làm staffId khi ghi NhatKyHoatDong (audit log), và kiểm tra vai trò/quyền sở hữu dữ liệu.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private static final String SYSTEM = "SYSTEM";

    private final UserRepository userRepository;

    public String resolveMaTK(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return SYSTEM;
        }
        return userRepository.findByTenDangNhap(authentication.getName())
                .map(User::getMaTK)
                .orElse(SYSTEM);
    }

    public boolean hasRole(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    /** ADMIN/ORGANIZER được xem/sửa toàn bộ dữ liệu, bỏ qua kiểm tra chủ sở hữu. */
    public boolean isPrivileged(Authentication authentication) {
        return hasRole(authentication, "ADMIN") || hasRole(authentication, "ORGANIZER");
    }
}
