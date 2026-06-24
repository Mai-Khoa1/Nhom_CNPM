package com.horseracing.dto.jockey;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO cập nhật chỉ số sức khỏe / thống kê của jockey
 * Theo sơ đồ tuần tự: updateStats(jockeyId, statsDTO)
 */
@Data
public class JockeyStatsDTO {

    @DecimalMin(value = "30.0", message = "Cân nặng tối thiểu 30 kg")
    @DecimalMax(value = "120.0", message = "Cân nặng tối đa 120 kg")
    private Double canNang;

    @DecimalMin(value = "10.0", message = "BMI không hợp lệ")
    @DecimalMax(value = "40.0", message = "BMI không hợp lệ")
    private Double bmi;

    @DecimalMin(value = "0.0", message = "Tỷ lệ thắng không được âm")
    @DecimalMax(value = "100.0", message = "Tỷ lệ thắng tối đa 100%")
    private Double tyLeThang;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String ghiChu;
}
