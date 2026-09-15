package com.agm.agrimitra.dto;

import com.agm.agrimitra.entity.SoilDataSource;
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

    @PositiveOrZero(message = "Electrical conductivity must be non-negative")
    private Double electricalConductivity;

    @PositiveOrZero(message = "Organic carbon must be non-negative")
    private Double organicCarbon;

    @PositiveOrZero(message = "Moisture level must be non-negative")
    private Double moistureLevel;

    @PositiveOrZero(message = "Sulphur level must be non-negative")
    private Double sulphurLevel;

    @PositiveOrZero(message = "Zinc level must be non-negative")
    private Double zincLevel;

    @PositiveOrZero(message = "Iron level must be non-negative")
    private Double ironLevel;

    @PositiveOrZero(message = "Copper level must be non-negative")
    private Double copperLevel;

    @PositiveOrZero(message = "Manganese level must be non-negative")
    private Double manganeseLevel;

    @PositiveOrZero(message = "Boron level must be non-negative")
    private Double boronLevel;

    @NotNull(message = "Tested date is required")
    private LocalDate testedDate;

    private String imageUrl;

    private SoilDataSource dataSource;
}
