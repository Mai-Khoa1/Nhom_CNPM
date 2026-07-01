package com.horseracing.dto.chungua;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChuNguaRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    private String diaChi;

    private String soDienThoai;

    private String email;
}
