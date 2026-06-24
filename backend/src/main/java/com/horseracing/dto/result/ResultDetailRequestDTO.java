package com.horseracing.dto.result;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDetailRequestDTO {

    @NotBlank(message = "Mã đăng ký không được để trống")
    private String registrationId;

    @NotNull(message = "Hạng về đích không được để trống")
    private Integer finishPosition;

    private String finishTime;

    private String notes;
}
