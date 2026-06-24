package com.horseracing.dto.user;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phone;
}
