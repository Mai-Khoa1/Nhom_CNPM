package com.horseracing.dto.updaterequest;

import com.horseracing.dto.common.UpdateRequestStatus;
import com.horseracing.dto.common.UpdateTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRequestResponseDTO {
    private String id;
    private UpdateTargetType targetType;
    private String targetId;
    private String targetName;
    private String ownerId;
    private String ownerName;
    private Map<String, Object> oldData;
    private Map<String, Object> newData;
    private UpdateRequestStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
