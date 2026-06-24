package com.horseracing.dto.horse;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới hoặc cập nhật ngựa đua
 */
@Data
public class HorseRequestDTO {

    @NotBlank(message = "Tên ngựa không được để trống")
    @Size(max = 100, message = "Tên ngựa tối đa 100 ký tự")
    private String tenNgua;

    @NotBlank(message = "Giống ngựa không được để trống")
    @Size(max = 50, message = "Giống ngựa tối đa 50 ký tự")
    private String giongNgua;

    private String ngaySinh; // yyyy-MM-dd

    private String gioiTinh; // "Đực" | "Cái"

    @Size(max = 30, message = "Màu lông tối đa 30 ký tự")
    private String mauLong;

    @DecimalMin(value = "0.0", inclusive = false, message = "Trọng lượng phải lớn hơn 0")
    private Double troiLuong;

    @Size(max = 255, message = "Tình trạng sức khỏe tối đa 255 ký tự")
    private String trangThaiSucKhoe;

    // "Đủ điều kiện" | "Chờ duyệt" | "Chấn thương" | "Bị loại"
    private String trangThai;

    @NotBlank(message = "Mã chủ ngựa không được để trống")
    private String maChuNgua;
}
