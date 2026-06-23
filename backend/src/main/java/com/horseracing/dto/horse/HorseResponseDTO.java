package com.horseracing.dto.horse;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin ngựa đua cho client
 */
@Data
@Builder
public class HorseResponseDTO {

    private String maNgua;
    private String tenNgua;
    private String giongNgua;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String mauLong;
    private Double troiLuong;
    private String trangThaiSucKhoe;
    private String trangThai;
    private String maChuNgua;
    private String tenChuNgua;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}
