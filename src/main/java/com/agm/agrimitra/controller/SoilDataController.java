package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.service.SoilDataService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Soil Data Controller", description = "Endpoints for managing soil test data")
@SecurityRequirement(name = "bearerAuth")
public class SoilDataController {

    private final SoilDataService soilDataService;

    @PostMapping("/api/fields/{fieldId}/soil-data")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Record new soil data for a specific field (Admin or field owner)")
    public ResponseEntity<SoilDataResponseDto> createSoilData(
            @PathVariable Long fieldId,
            @Valid @RequestBody SoilDataRequestDto requestDto) {
        SoilDataResponseDto createdSoilData = soilDataService.createSoilData(fieldId, requestDto);
        return new ResponseEntity<>(createdSoilData, HttpStatus.CREATED);
    }

    @GetMapping("/api/fields/{fieldId}/soil-data")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Get all soil data records for a specific field with pagination and sorting (Admin or field owner)")
    public ResponseEntity<Page<SoilDataResponseDto>> getSoilDataByFieldId(
            @PathVariable Long fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "testedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "testedDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));

        Page<SoilDataResponseDto> soilDataList = soilDataService.getSoilDataByFieldId(fieldId, pageable);
        return ResponseEntity.ok(soilDataList);
    }

    @GetMapping("/api/soil-data/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSoilDataOwner(#id)")
    @Operation(summary = "Get soil data record by ID (Admin or owner)")
    public ResponseEntity<SoilDataResponseDto> getSoilDataById(@PathVariable Long id) {
        SoilDataResponseDto soilData = soilDataService.getSoilDataById(id);
        return ResponseEntity.ok(soilData);
    }

    @DeleteMapping("/api/soil-data/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isSoilDataOwner(#id)")
    @Operation(summary = "Delete soil data record by ID (Admin or owner)")
    public ResponseEntity<Void> deleteSoilData(@PathVariable Long id) {
        soilDataService.deleteSoilData(id);
        return ResponseEntity.noContent().build();
    }
}
