package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.service.CropRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Crop Recommendation Controller", description = "Endpoints for retrieving and deleting crop recommendations")
@SecurityRequirement(name = "bearerAuth")
public class CropRecommendationController {

    private final CropRecommendationService cropRecommendationService;

    @GetMapping("/api/fields/{fieldId}/recommendations")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Get all crop recommendations for a specific field with pagination and sorting (Admin or field owner)")
    public ResponseEntity<Page<CropRecommendationResponseDto>> getRecommendationsByFieldId(
            @PathVariable Long fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recommendationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "recommendationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));

        Page<CropRecommendationResponseDto> recommendations =
                cropRecommendationService.getRecommendationsByFieldId(fieldId, pageable);
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/api/recommendations/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isRecommendationOwner(#id)")
    @Operation(summary = "Get crop recommendation by ID (Admin or field owner)")
    public ResponseEntity<CropRecommendationResponseDto> getRecommendationById(@PathVariable Long id) {
        CropRecommendationResponseDto recommendation =
                cropRecommendationService.getRecommendationById(id);
        return ResponseEntity.ok(recommendation);
    }

    @DeleteMapping("/api/recommendations/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isRecommendationOwner(#id)")
    @Operation(summary = "Delete crop recommendation by ID (Admin or field owner)")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        cropRecommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }
}
