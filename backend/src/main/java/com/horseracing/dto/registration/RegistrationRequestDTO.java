package com.horseracing.dto.registration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDTO {

    @NotBlank(message = "Mã chặng đua không được để trống")
    private String raceId;

    @NotBlank(message = "Mã ngựa không được để trống")
    private String horseId;

    @NotBlank(message = "Mã jockey không được để trống")
    private String jockeyId;
}
