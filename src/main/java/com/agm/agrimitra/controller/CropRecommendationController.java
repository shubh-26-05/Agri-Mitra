package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.service.CropRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Crop Recommendation Controller", description = "Endpoints for retrieving and deleting crop recommendations")
public class CropRecommendationController {

    private final CropRecommendationService cropRecommendationService;

    @GetMapping("/api/fields/{fieldId}/recommendations")
    @Operation(summary = "Get all crop recommendations for a specific field")
    public ResponseEntity<List<CropRecommendationResponseDto>> getRecommendationsByFieldId(
            @PathVariable Long fieldId) {
        List<CropRecommendationResponseDto> recommendations =
                cropRecommendationService.getRecommendationsByFieldId(fieldId);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/api/recommendations/{id}")
    @Operation(summary = "Get crop recommendation by ID")
    public ResponseEntity<CropRecommendationResponseDto> getRecommendationById(@PathVariable Long id) {
        CropRecommendationResponseDto recommendation =
                cropRecommendationService.getRecommendationById(id);
        return ResponseEntity.ok(recommendation);
    }

    @DeleteMapping("/api/recommendations/{id}")
    @Operation(summary = "Delete crop recommendation by ID")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        cropRecommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }
}
