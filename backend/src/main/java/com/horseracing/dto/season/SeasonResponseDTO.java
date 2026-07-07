package com.horseracing.dto.season;

import com.horseracing.dto.common.SeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonResponseDTO {
    private String id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private SeasonStatus status;
    private String description;
    private String organizerId;
    private String organizerName;
    private String createdBy;
    private LocalDateTime createdAt;
}
