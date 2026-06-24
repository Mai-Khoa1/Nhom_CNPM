package com.horseracing.dto.jockey;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO nhận dữ liệu khi tạo mới hoặc cập nhật jockey (nài ngựa)
 */
@Data
public class JockeyRequestDTO {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    private String ngaySinh; // yyyy-MM-dd

    @Size(max = 50, message = "Quốc tịch tối đa 50 ký tự")
    private String quocTich;

    @Min(value = 0, message = "Kinh nghiệm không được âm")
    private Integer kinhNghiem;

    @NotBlank(message = "Số giấy phép không được để trống")
    @Size(max = 50, message = "Số giấy phép tối đa 50 ký tự")
    private String soGiayPhep;

    // "Sẵn sàng" | "Chấn thương" | "Nghỉ hưu"
    private String trangThai;

    @NotBlank(message = "Mã chủ ngựa không được để trống")
    private String maChuNgua;
}
