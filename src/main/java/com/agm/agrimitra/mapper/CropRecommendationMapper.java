package com.agm.agrimitra.mapper;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.entity.CropRecommendation;
import org.springframework.stereotype.Component;

@Component
public class CropRecommendationMapper {

    public CropRecommendationResponseDto toResponseDto(CropRecommendation entity) {
        if (entity == null) {
            return null;
        }

        Long fieldId = entity.getField() != null ? entity.getField().getId() : null;
        Long soilDataId = entity.getSoilData() != null ? entity.getSoilData().getId() : null;

        return CropRecommendationResponseDto.builder()
                .id(entity.getId())
                .fieldId(fieldId)
                .soilDataId(soilDataId)
                .recommendedCrop(entity.getRecommendedCrop())
                .confidenceScore(entity.getConfidenceScore())
                .reasoning(entity.getReasoning())
                .fertilizerAdvice(entity.getFertilizerAdvice())
                .recommendationDate(entity.getRecommendationDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
