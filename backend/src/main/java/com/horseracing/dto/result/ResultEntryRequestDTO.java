package com.horseracing.dto.result;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntryRequestDTO {

    @NotBlank(message = "Mã chặng đua không được để trống")
    private String raceId;

    @NotEmpty(message = "Danh sách kết quả không được để trống")
    @Valid
    private List<ResultDetailRequestDTO> details;

    /** Phải = true mới được sửa kết quả của 1 chặng đua đã công bố (xác nhận rõ ràng từ Ban tổ chức). */
    private boolean confirmEditPublished;
}
