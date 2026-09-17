package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.service.FertilizerUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Fertilizer Usage Controller", description = "Endpoints for tracking fertilizer application history")
@SecurityRequirement(name = "bearerAuth")
public class FertilizerUsageController {

    private final FertilizerUsageService fertilizerUsageService;

    @PostMapping("/api/fields/{fieldId}/fertilizer-usage")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Log fertilizer usage for a specific field (Admin or field owner)")
    public ResponseEntity<FertilizerUsageResponseDto> createFertilizerUsage(
            @PathVariable Long fieldId,
            @Valid @RequestBody FertilizerUsageRequestDto requestDto,
            @AuthenticationPrincipal User currentUser) {
        Long authenticatedUserId = (currentUser != null) ? currentUser.getId() : null;
        FertilizerUsageResponseDto created = fertilizerUsageService.createFertilizerUsage(fieldId, requestDto, authenticatedUserId);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/api/fields/{fieldId}/fertilizer-usage")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Get all fertilizer usage records for a specific field with pagination and sorting (Admin or field owner)")
    public ResponseEntity<Page<FertilizerUsageResponseDto>> getFertilizerUsageByFieldId(
            @PathVariable Long fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "applicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "applicationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));

        Long authenticatedUserId = (currentUser != null) ? currentUser.getId() : null;
        Page<FertilizerUsageResponseDto> usageList = fertilizerUsageService.getFertilizerUsageHistory(fieldId, pageable, authenticatedUserId);
        return ResponseEntity.ok(usageList);
    }

    @DeleteMapping("/api/fertilizer-usage/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFertilizerUsageOwner(#id)")
    @Operation(summary = "Delete fertilizer usage record by ID (Admin or field owner)")
    public ResponseEntity<Void> deleteFertilizerUsage(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        Long authenticatedUserId = (currentUser != null) ? currentUser.getId() : null;
        fertilizerUsageService.deleteFertilizerUsage(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
