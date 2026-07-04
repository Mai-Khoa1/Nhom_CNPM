package com.horseracing.dto.user;

import com.horseracing.validation.ValidEmailFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDTO {

    private String fullName;

    @ValidEmailFormat
    private String email;

    private String phone;
}
