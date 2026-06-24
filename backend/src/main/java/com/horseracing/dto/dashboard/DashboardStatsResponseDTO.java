package com.horseracing.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponseDTO {
    private long totalHorses;
    private long totalJockeys;
    private long totalRaces;
    private long totalSeasons;
    private long pendingHorses;
    private long pendingJockeys;
    private long pendingRegistrations;
    private long upcomingRaces;
}
