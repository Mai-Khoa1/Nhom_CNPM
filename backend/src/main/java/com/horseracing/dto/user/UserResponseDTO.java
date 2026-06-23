package com.horseracing.dto.user;

import com.horseracing.dto.common.Role;
import com.horseracing.dto.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private String id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
