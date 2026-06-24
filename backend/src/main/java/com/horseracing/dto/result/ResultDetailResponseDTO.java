package com.horseracing.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultDetailResponseDTO {
    private Integer finishPosition;
    private String horseId;
    private String horseName;
    private String horseCode;
    private String jockeyId;
    private String jockeyName;
    private Integer laneNumber;
    private String finishTime;
    private Double pointsEarned;
    private String notes;
}
