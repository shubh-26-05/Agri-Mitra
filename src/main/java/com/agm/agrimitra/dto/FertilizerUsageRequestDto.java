package com.agm.agrimitra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FertilizerUsageRequestDto {

    @NotBlank(message = "Fertilizer type is required")
    private String fertilizerType;

    @NotNull(message = "Quantity used is required")
    @Positive(message = "Quantity used must be greater than zero")
    private Double quantityUsed;

    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;

    @NotNull(message = "Application date is required")
    @PastOrPresent(message = "Application date cannot be in the future")
    private LocalDate applicationDate;
}
