package com.horseracing.dto.upload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TepTinRequestDTO - dữ liệu tạo mới/sửa metadata tệp tin (dùng làm snapshot cũ/mới trong YeuCauCapNhat).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TepTinRequestDTO {

    @NotBlank(message = "Tên file không được để trống")
    private String tenFile;

    private String loaiFile;

    /** "DANG_KY" - tệp tin gắn với 1 lần đăng ký thi đấu cụ thể (maDangKy) */
    private String targetType;

    private String targetId;
}
