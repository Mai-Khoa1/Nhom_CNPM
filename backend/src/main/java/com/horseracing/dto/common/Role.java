package com.horseracing.dto.common;

/**
 * Vai trò người dùng ở tầng API (tiếng Anh, khớp với frontend).
 * Map 1-1 với cột vaiTro tiếng Việt trong bảng TaiKhoan thông qua RoleMapper.
 */
public enum Role {
    ADMIN,
    ORGANIZER,
    HORSE_OWNER,
    SPECTATOR
}
