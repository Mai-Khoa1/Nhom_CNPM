package com.horseracing.dto.season;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointRuleRequestDTO {

    @NotNull(message = "Hạng không được để trống")
    private Integer position;

    @NotNull(message = "Điểm không được để trống")
    private Double point;
}
