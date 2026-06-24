package com.horseracing.dto.chungua;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới hoặc cập nhật chủ ngựa
 */
@Data
public class ChuNguaRequestDTO {

    @NotBlank(message = "Mã chủ ngựa không được để trống")
    @Size(max = 50, message = "Mã chủ ngựa tối đa 50 ký tự")
    private String maChuNgua;

    @NotBlank(message = "Mã tài khoản không được để trống")
    @Size(max = 50, message = "Mã tài khoản tối đa 50 ký tự")
    private String maTK;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;
}
