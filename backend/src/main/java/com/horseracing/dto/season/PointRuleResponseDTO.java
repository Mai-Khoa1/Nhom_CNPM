package com.horseracing.dto.season;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointRuleResponseDTO {
    private String id;
    private String seasonId;
    private Integer position;
    private Double point;
}
