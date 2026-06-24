package com.horseracing.dto.nhatky;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKyHoatDongResponseDTO {
    private String maNhatKy;
    private String maTK;
    private String loaiHanhDong;
    private String moTa;
    private LocalDateTime thoiGian;
    private String diaChiIP;
    private String doiTuongTacDong;
}
