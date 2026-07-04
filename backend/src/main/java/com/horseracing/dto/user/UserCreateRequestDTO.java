package com.horseracing.dto.user;

import com.horseracing.dto.common.Role;
import com.horseracing.validation.ValidEmailFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dùng khi Admin tạo tài khoản mới (có chọn role), khác với RegisterRequest công khai (luôn là SPECTATOR). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @ValidEmailFormat
    private String email;

    private String phone;

    @NotNull(message = "Vai trò không được để trống")
    private Role role;
}
