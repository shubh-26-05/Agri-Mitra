package com.agm.agrimitra.dto;

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
public class FertilizerUsageResponseDto {

    private Long id;
    private Long fieldId;
    private String fertilizerType;
    private Double quantityUsed;
    private String quantityUnit;
    private LocalDate applicationDate;
    private LocalDateTime createdAt;
}
