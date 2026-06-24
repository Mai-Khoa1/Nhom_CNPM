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
public class HorseRankingResponseDTO {
    private String id;
    private String seasonId;
    private String horseId;
    private String horseName;
    private String horseCode;
    private String ownerName;
    private Double totalPoints;
    private Long totalRaces;
    private Long totalWins;
    private LocalDateTime updatedAt;
}
