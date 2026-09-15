package com.agm.agrimitra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldResponseDto {

    private Long id;
    private String fieldName;
    private Double areaInAcres;
    private Long farmerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
