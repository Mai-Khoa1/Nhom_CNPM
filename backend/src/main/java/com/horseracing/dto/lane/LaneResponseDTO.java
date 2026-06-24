package com.horseracing.dto.lane;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaneResponseDTO {
    private String id;
    private String raceId;
    private String registrationId;
    private String horseName;
    private String jockeyName;
    private Integer laneNumber;
    private LocalDateTime assignedAt;
}
