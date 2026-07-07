package com.horseracing.dto.season;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeasonRequestDTO {

    @NotBlank(message = "Tên mùa giải không được để trống")
    private String name;

    /** Định dạng ISO yyyy-MM-dd */
    private String startDate;

    private String endDate;

    private String description;

    /** Chỉ dùng khi ADMIN tạo mùa giải (ADMIN không có hồ sơ Ban tổ chức riêng nên phải chỉ định).
     *  Khi người tạo là ORGANIZER, giá trị này bị bỏ qua - hệ thống tự gán theo hồ sơ BanToChuc của người tạo. */
    private String organizerId;
}
