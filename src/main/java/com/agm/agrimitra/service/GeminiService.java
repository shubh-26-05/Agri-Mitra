package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.SoilExtractionResult;

public interface GeminiService {

    SoilExtractionResult extractSoilDataFromImage(byte[] imageBytes, String mimeType);

    SoilExtractionResult estimateSoilDataFromRegion(String village, String district, String state);
}
