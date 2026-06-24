package com.horseracing.dto.common;

/**
 * Trạng thái tài khoản ở tầng API (tiếng Anh, khớp với frontend).
 * Map 1-1 với cột trangThai tiếng Việt trong bảng TaiKhoan thông qua RoleMapper.
 */
public enum UserStatus {
    ACTIVE,
    LOCKED
}
