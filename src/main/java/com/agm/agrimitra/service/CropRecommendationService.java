package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CropRecommendationService {

    Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, Pageable pageable);

    Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir);

    CropRecommendationResponseDto getRecommendationById(Long id);

    void deleteRecommendation(Long id);

    CropRecommendationResponseDto generateCropRecommendationForField(Long fieldId, Long authenticatedUserId);

    CropRecommendationResponseDto generateCropRecommendationForField(Long fieldId);
}
