package com.horseracing.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequestDTO {

    /** Bỏ trống sẽ dùng mùa giải mặc định */
    private String seasonId;

    @NotBlank(message = "Tên chặng đua không được để trống")
    private String name;

    /** Định dạng ISO datetime yyyy-MM-ddTHH:mm:ss */
    private String raceDate;

    private String location;

    private Integer distance;

    private Integer maxHorses;

    private String description;
}
