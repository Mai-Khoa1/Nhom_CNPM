package com.horseracing.dto.ngua;

import com.horseracing.dto.common.Gender;
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
public class NguaResponseDTO {
    private String id;
    private String code;
    private String name;
    private String breed;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String color;
    private Double weight;
    private String avatarUrl;
    private String passportUrl;
    private String healthCertUrl;
    private String ownerId;
    private String ownerName;
    /** Lỗi 6 (soft-delete): false = "Ngừng hoạt động" - hồ sơ đã từng có đăng ký nên không xóa cứng được. */
    private boolean active;
    private LocalDateTime createdAt;
}
