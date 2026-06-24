package com.horseracing.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultResponseDTO {
    private String id;
    private String raceId;
    private String raceName;
    private boolean isPublished;
    private LocalDateTime publishedAt;
    private List<ResultDetailResponseDTO> details;
}
