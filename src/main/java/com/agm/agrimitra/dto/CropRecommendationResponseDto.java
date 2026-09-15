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
public class CropRecommendationResponseDto {

    private Long id;
    private Long fieldId;
    private Long soilDataId;
    private String recommendedCrop;
    private Double confidenceScore;
    private LocalDateTime recommendationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
