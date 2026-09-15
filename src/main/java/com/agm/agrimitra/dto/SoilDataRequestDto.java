package com.agm.agrimitra.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilDataRequestDto {

    @NotNull(message = "Nitrogen level is required")
    @PositiveOrZero(message = "Nitrogen level must be non-negative")
    private Double nitrogenLevel;

    @NotNull(message = "Phosphorus level is required")
    @PositiveOrZero(message = "Phosphorus level must be non-negative")
    private Double phosphorusLevel;

    @NotNull(message = "Potassium level is required")
    @PositiveOrZero(message = "Potassium level must be non-negative")
    private Double potassiumLevel;

    @NotNull(message = "pH level is required")
    @Positive(message = "pH level must be greater than zero")
    private Double phLevel;

    @PositiveOrZero(message = "Moisture level must be non-negative")
    private Double moistureLevel;

    @NotNull(message = "Tested date is required")
    private LocalDate testedDate;
}
