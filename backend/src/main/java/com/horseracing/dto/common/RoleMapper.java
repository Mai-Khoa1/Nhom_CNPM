package com.horseracing.dto.common;

import com.horseracing.entity.User;
import com.horseracing.exception.ResourceNotFoundException;

/**
 * Chuyển đổi 2 chiều giữa vaiTro/trangThai tiếng Việt (DB) và Role/UserStatus tiếng Anh (API).
 */
public final class RoleMapper {

    private RoleMapper() {
    }

    public static Role toRole(String vaiTro) {
        if (vaiTro == null) return null;
        return switch (vaiTro) {
            case User.VAI_TRO_ADMIN -> Role.ADMIN;
            case User.VAI_TRO_BAN_TO_CHUC -> Role.ORGANIZER;
            case User.VAI_TRO_CHU_NGUA -> Role.HORSE_OWNER;
            case User.VAI_TRO_NGUOI_XEM -> Role.SPECTATOR;
            default -> throw new IllegalArgumentException("Vai trò không hợp lệ: " + vaiTro);
        };
    }

    public static String toVaiTro(Role role) {
        if (role == null) return User.VAI_TRO_NGUOI_XEM;
        return switch (role) {
            case ADMIN -> User.VAI_TRO_ADMIN;
            case ORGANIZER -> User.VAI_TRO_BAN_TO_CHUC;
            case HORSE_OWNER -> User.VAI_TRO_CHU_NGUA;
            case SPECTATOR -> User.VAI_TRO_NGUOI_XEM;
        };
    }

    public static UserStatus toUserStatus(String trangThai) {
        if (trangThai == null) return UserStatus.ACTIVE;
        return switch (trangThai) {
            case User.TRANG_THAI_HOAT_DONG -> UserStatus.ACTIVE;
            case User.TRANG_THAI_BI_KHOA -> UserStatus.LOCKED;
            default -> throw new ResourceNotFoundException("Trạng thái không hợp lệ: " + trangThai);
        };
    }

    public static String toTrangThai(UserStatus status) {
        if (status == null) return User.TRANG_THAI_HOAT_DONG;
        return switch (status) {
            case ACTIVE -> User.TRANG_THAI_HOAT_DONG;
            case LOCKED -> User.TRANG_THAI_BI_KHOA;
        };
    }

    /** Tên quyền Spring Security tương ứng (dùng cho @PreAuthorize, hasRole...) */
    public static String toAuthority(Role role) {
        return "ROLE_" + role.name();
    }
}
