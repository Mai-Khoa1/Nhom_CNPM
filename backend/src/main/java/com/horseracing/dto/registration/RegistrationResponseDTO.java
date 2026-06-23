package com.horseracing.dto.registration;

import com.horseracing.dto.common.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponseDTO {
    private String id;
    private String raceId;
    private String raceName;
    private LocalDateTime raceDate;
    private String horseId;
    private String horseName;
    private String horseCode;
    private String jockeyId;
    private String jockeyName;
    private String ownerId;
    private String ownerName;
    private Integer laneNumber;
    private RegistrationStatus status;
    private String rejectReason;
    private LocalDateTime registeredAt;
}
