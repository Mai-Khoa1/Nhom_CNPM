package com.horseracing.dto.nainghua;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaiNguaRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    /** Định dạng ISO yyyy-MM-dd */
    private String dateOfBirth;

    /** "MALE" hoặc "FEMALE" */
    private String gender;

    private Integer experienceYears;

    private Double weight;

    private String licenseNumber;
}
