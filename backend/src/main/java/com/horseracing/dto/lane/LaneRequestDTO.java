package com.horseracing.dto.lane;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaneRequestDTO {

    @NotBlank(message = "Mã đăng ký không được để trống")
    private String registrationId;

    @NotNull(message = "Số làn đua không được để trống")
    private Integer laneNumber;
}
