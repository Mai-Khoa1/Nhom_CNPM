package com.horseracing.dto.chungua;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin chủ ngựa cho client
 */
@Data
@Builder
public class ChuNguaResponseDTO {

    private String maChuNgua;
    private String maTK;
    private String hoTen;
    private String diaChi;
    private String soDienThoai;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
