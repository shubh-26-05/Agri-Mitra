package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;

import java.util.List;

public interface CropRecommendationService {

    List<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId);

    CropRecommendationResponseDto getRecommendationById(Long id);

    void deleteRecommendation(Long id);
}
