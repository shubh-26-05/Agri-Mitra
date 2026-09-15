package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.dto.SoilExtractionResult;
import com.agm.agrimitra.entity.Address;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.entity.SoilDataSource;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.SoilDataMapper;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.SoilDataRepository;
import com.agm.agrimitra.service.GeminiService;
import com.agm.agrimitra.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Tag(name = "Soil Card Controller", description = "Endpoints for Soil Health Card upload, OCR extraction, and regional web fallback")
@SecurityRequirement(name = "bearerAuth")
public class SoilCardController {

    private final S3Service s3Service;
    private final GeminiService geminiService;
    private final FieldRepository fieldRepository;
    private final SoilDataRepository soilDataRepository;
    private final SoilDataMapper soilDataMapper;

    @PostMapping(value = "/api/fields/{fieldId}/soil-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#fieldId)")
    @Operation(summary = "Upload Soil Health Card image with vision OCR and automatic regional web fallback")
    public ResponseEntity<SoilDataResponseDto> uploadAndExtractSoilCard(
            @PathVariable Long fieldId,
            @RequestParam("file") MultipartFile file) {

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        // 1. Validate file and upload to S3 via S3Service
        String s3Key = s3Service.uploadFile(file, fieldId);

        // 2. Read file bytes and call Gemini OCR
        byte[] imageBytes;
        try {
            imageBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image bytes: " + e.getMessage(), e);
        }

        SoilExtractionResult extractionResult = geminiService.extractSoilDataFromImage(imageBytes, file.getContentType());

        // 3. Check OCR success; if false, trigger automatic web-search fallback
        boolean ocrSuccess = Boolean.TRUE.equals(extractionResult.getExtractionSuccessful());
        SoilDataSource dataSource;
        SoilExtractionResult finalResult;

        if (ocrSuccess) {
            dataSource = SoilDataSource.OCR_EXTRACTED;
            finalResult = extractionResult;
        } else {
            // OCR failed: look up Field's owning Farmer location and query regional estimates
            Address location = (field.getFarmer() != null) ? field.getFarmer().getLocation() : null;
            String village = (location != null) ? location.getVillage() : null;
            String district = (location != null) ? location.getDistrict() : null;
            String state = (location != null) ? location.getState() : null;

            SoilExtractionResult fallbackResult = geminiService.estimateSoilDataFromRegion(village, district, state);
            finalResult = (fallbackResult != null) ? fallbackResult : SoilExtractionResult.builder().extractionSuccessful(false).build();
            dataSource = SoilDataSource.WEB_FALLBACK;
        }

        // 4. Map result into new SoilData entity
        SoilData soilData = SoilData.builder()
                .field(field)
                .nitrogenLevel(finalResult.getNitrogenLevel())
                .phosphorusLevel(finalResult.getPhosphorusLevel())
                .potassiumLevel(finalResult.getPotassiumLevel())
                .phLevel(finalResult.getPhLevel())
                .electricalConductivity(finalResult.getElectricalConductivity())
                .organicCarbon(finalResult.getOrganicCarbon())
                .sulphurLevel(finalResult.getSulphurLevel())
                .zincLevel(finalResult.getZincLevel())
                .ironLevel(finalResult.getIronLevel())
                .copperLevel(finalResult.getCopperLevel())
                .manganeseLevel(finalResult.getManganeseLevel())
                .boronLevel(finalResult.getBoronLevel())
                .testedDate(LocalDate.now())
                .imageUrl(s3Key)
                .dataSource(dataSource)
                .build();

        // 5. Save via SoilDataRepository
        SoilData saved = soilDataRepository.save(soilData);

        // 6. Return response DTO
        return new ResponseEntity<>(soilDataMapper.toResponseDto(saved), HttpStatus.CREATED);
    }
}
