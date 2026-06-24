package com.horseracing.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JockeyRankingResponseDTO {
    private String id;
    private String seasonId;
    private String jockeyId;
    private String jockeyName;
    private String ownerName;
    private Double totalPoints;
    private Long totalRaces;
    private Long totalWins;
    private LocalDateTime updatedAt;
}
