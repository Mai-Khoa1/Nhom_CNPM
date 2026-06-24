package com.horseracing.dto.user;

import com.horseracing.dto.common.RoleMapper;
import com.horseracing.entity.User;

/**
 * Chuyển đổi entity User (bảng TaiKhoan, field tiếng Việt) sang UserResponseDTO (field tiếng Anh khớp frontend).
 */
public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getMaTK())
                .username(user.getTenDangNhap())
                .fullName(user.getHoTen())
                .email(user.getEmail())
                .phone(user.getSoDienThoai())
                .role(RoleMapper.toRole(user.getVaiTro()))
                .status(RoleMapper.toUserStatus(user.getTrangThai()))
                .createdAt(user.getNgayTao())
                .build();
    }
}
