package com.horseracing.dto.ngua;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NguaRequestDTO {

    /** Mã ngựa tùy chọn do người dùng đặt khi tạo mới; bỏ trống sẽ tự sinh */
    private String code;

    @NotBlank(message = "Tên ngựa không được để trống")
    private String name;

    private String breed;

    /** Định dạng ISO yyyy-MM-dd */
    private String dateOfBirth;

    /** "MALE" hoặc "FEMALE" */
    private String gender;

    private String color;

    private Double weight;
}
