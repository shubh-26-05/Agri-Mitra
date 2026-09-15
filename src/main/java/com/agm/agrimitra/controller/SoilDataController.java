package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.service.SoilDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Soil Data Controller", description = "Endpoints for managing soil test data")
public class SoilDataController {

    private final SoilDataService soilDataService;

    @PostMapping("/api/fields/{fieldId}/soil-data")
    @Operation(summary = "Record new soil data for a specific field")
    public ResponseEntity<SoilDataResponseDto> createSoilData(
            @PathVariable Long fieldId,
            @Valid @RequestBody SoilDataRequestDto requestDto) {
        SoilDataResponseDto createdSoilData = soilDataService.createSoilData(fieldId, requestDto);
        return new ResponseEntity<>(createdSoilData, HttpStatus.CREATED);
    }

    @GetMapping("/api/fields/{fieldId}/soil-data")
    @Operation(summary = "Get all soil data records for a specific field")
    public ResponseEntity<List<SoilDataResponseDto>> getSoilDataByFieldId(@PathVariable Long fieldId) {
        List<SoilDataResponseDto> soilDataList = soilDataService.getSoilDataByFieldId(fieldId);
        return ResponseEntity.ok(soilDataList);
    }

    @GetMapping("/api/soil-data/{id}")
    @Operation(summary = "Get soil data record by ID")
    public ResponseEntity<SoilDataResponseDto> getSoilDataById(@PathVariable Long id) {
        SoilDataResponseDto soilData = soilDataService.getSoilDataById(id);
        return ResponseEntity.ok(soilData);
    }

    @DeleteMapping("/api/soil-data/{id}")
    @Operation(summary = "Delete soil data record by ID")
    public ResponseEntity<Void> deleteSoilData(@PathVariable Long id) {
        soilDataService.deleteSoilData(id);
        return ResponseEntity.noContent().build();
    }
}
