package com.agm.agrimitra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropRecommendationResult {

    private String recommendedCrop;
    private Double confidenceScore;
    private String reasoning;
    private String fertilizerAdvice;
}
