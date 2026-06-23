package com.horseracing.service;

import com.horseracing.entity.User;
import com.horseracing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Tiện ích lấy maTK (mã tài khoản) của người dùng đang đăng nhập từ Authentication,
 * dùng làm staffId khi ghi NhatKyHoatDong (audit log).
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
}
