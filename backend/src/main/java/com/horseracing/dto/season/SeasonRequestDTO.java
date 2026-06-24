package com.horseracing.dto.season;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRequestDTO {

    @NotBlank(message = "Tên mùa giải không được để trống")
    private String name;

    /** Định dạng ISO yyyy-MM-dd */
    private String startDate;

    private String endDate;

    private String description;
}
