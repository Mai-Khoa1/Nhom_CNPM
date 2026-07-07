package com.horseracing.dto.schedule;

import com.horseracing.dto.common.RaceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
    private String id;
    private String seasonId;
    private String seasonName;
    private String name;
    private LocalDateTime raceDate;
    private String location;
    private Integer distance;
    private Integer maxHorses;
    private Integer minHorses;
    private long registeredCount;
    private long approvedCount;
    private RaceStatus status;
    private String description;
    private LocalDateTime createdAt;
}
