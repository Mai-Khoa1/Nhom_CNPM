package com.horseracing.dto.nainghua;

import com.horseracing.dto.common.Gender;
import com.horseracing.dto.common.JockeyStatus;
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
public class NaiNguaResponseDTO {
    private String id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Integer experienceYears;
    private Double weight;
    private String licenseNumber;
    private String ownerId;
    private String ownerName;
    private String avatarUrl;
    private String licenseScanUrl;
    private String medicalCertUrl;
    private JockeyStatus status;
    private LocalDateTime createdAt;
}
