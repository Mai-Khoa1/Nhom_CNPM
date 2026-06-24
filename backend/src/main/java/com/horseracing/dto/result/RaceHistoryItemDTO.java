package com.horseracing.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Một dòng lịch sử thi đấu của một con ngựa (dùng cho GET /horses/{id}/race-history). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceHistoryItemDTO {
    private String raceId;
    private String raceName;
    private Integer finishPosition;
    private String finishTime;
    private Double pointsEarned;
    private boolean published;
    private LocalDateTime publishedAt;
}
