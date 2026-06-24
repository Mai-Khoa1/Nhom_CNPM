package com.horseracing.dto.jockey;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO trả về thông tin jockey cho client
 */
@Data
@Builder
public class JockeyResponseDTO {

    private String maNaiNgua;
    private String hoTen;
    private LocalDate ngaySinh;
    private String quocTich;
    private Integer kinhNghiem;
    private String soGiayPhep;
    private String trangThai;
    private String maChuNgua;
    private String tenChuNgua;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}
