package com.horseracing.dto.chungua;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuNguaResponseDTO {
    private String maChuNgua;
    private String maTK;
    private String hoTen;
    private String diaChi;
    private String soDienThoai;
    private String email;
}
