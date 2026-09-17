package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.CropRecommendationResult;
import com.agm.agrimitra.dto.SoilExtractionResult;
import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.SoilData;

import java.util.List;

public interface GeminiService {

    SoilExtractionResult extractSoilDataFromImage(byte[] imageBytes, String mimeType);

    SoilExtractionResult estimateSoilDataFromRegion(String village, String district, String state);

    CropRecommendationResult generateCropRecommendation(
            SoilData soilData,
            WeatherDataDto weather,
            List<FertilizerUsage> recentFertilizerHistory,
            Double fieldAreaInAcres,
            String stalenessTier);
}
