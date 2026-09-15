package com.agm.agrimitra.dto;

import com.agm.agrimitra.entity.SoilDataSource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilDataResponseDto {

    private Long id;
    private Long fieldId;
    private Double nitrogenLevel;
    private Double phosphorusLevel;
    private Double potassiumLevel;
    private Double phLevel;
    private Double electricalConductivity;
    private Double organicCarbon;
    private Double moistureLevel;
    private Double sulphurLevel;
    private Double zincLevel;
    private Double ironLevel;
    private Double copperLevel;
    private Double manganeseLevel;
    private Double boronLevel;
    private LocalDate testedDate;
    private String imageUrl;
    private SoilDataSource dataSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
