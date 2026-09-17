package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.dto.CropRecommendationResult;
import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.entity.CropRecommendation;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.Role;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.entity.SoilStalenessTier;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.exception.UnprocessableEntityException;
import com.agm.agrimitra.mapper.CropRecommendationMapper;
import com.agm.agrimitra.repository.CropRecommendationRepository;
import com.agm.agrimitra.repository.FertilizerUsageRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.UserRepository;
import com.agm.agrimitra.service.CropRecommendationService;
import com.agm.agrimitra.service.GeminiService;
import com.agm.agrimitra.service.SoilDataService;
import com.agm.agrimitra.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CropRecommendationServiceImpl implements CropRecommendationService {

    private final CropRecommendationRepository cropRecommendationRepository;
    private final FieldRepository fieldRepository;
    private final CropRecommendationMapper cropRecommendationMapper;
    private final SoilDataService soilDataService;
    private final WeatherService weatherService;
    private final FertilizerUsageRepository fertilizerUsageRepository;
    private final GeminiService geminiService;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, Pageable pageable) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResourceNotFoundException("Field", "id", fieldId);
        }

        return cropRecommendationRepository.findByFieldId(fieldId, pageable)
                .map(cropRecommendationMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir) {
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "recommendationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));
        return getRecommendationsByFieldId(fieldId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CropRecommendationResponseDto getRecommendationById(Long id) {
        CropRecommendation recommendation = cropRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CropRecommendation", "id", id));
        return cropRecommendationMapper.toResponseDto(recommendation);
    }

    @Override
    public void deleteRecommendation(Long id) {
        if (!cropRecommendationRepository.existsById(id)) {
            throw new ResourceNotFoundException("CropRecommendation", "id", id);
        }
        cropRecommendationRepository.deleteById(id);
    }

    @Override
    public CropRecommendationResponseDto generateCropRecommendationForField(Long fieldId, Long authenticatedUserId) {
        // 1. Ownership check & field existence
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));
        checkFieldAccess(field, authenticatedUserId);

        // 2. Fetch field's latest SoilData record
        SoilData latestSoilData = soilDataService.getLatestSoilDataForField(fieldId)
                .orElseThrow(() -> new UnprocessableEntityException(
                        "A soil card upload is required first before generating crop recommendations."));

        // 3. Compute staleness tier
        SoilStalenessTier stalenessTier = soilDataService.calculateStalenessTier(latestSoilData);
        if (stalenessTier == SoilStalenessTier.CRITICAL_STALE) {
            throw new UnprocessableEntityException(
                    "Soil data is over 36 months old. A fresh soil card upload is required before a recommendation can be generated.");
        }

        // 4. Fetch current weather via WeatherService using owning farmer's district/state
        WeatherDataDto weather = null;
        try {
            Farmer farmer = field.getFarmer();
            if (farmer != null && farmer.getLocation() != null) {
                String district = farmer.getLocation().getDistrict();
                String state = farmer.getLocation().getState();
                if (district != null && !district.isBlank() && state != null && !state.isBlank()) {
                    weather = weatherService.getCurrentWeather(district, state).orElse(null);
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve current weather for field {}: {}", fieldId, e.getMessage());
        }

        // 5. Fetch most recent fertilizer usage history (last 5 records)
        Pageable recentLimit = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "applicationDate"));
        Page<FertilizerUsage> usages = fertilizerUsageRepository.findByFieldIdOrderByApplicationDateDesc(fieldId, recentLimit);
        List<FertilizerUsage> recentFertilizer = (usages != null && usages.hasContent()) ? usages.getContent() : Collections.emptyList();

        // 6. Call GeminiService
        CropRecommendationResult geminiResult = geminiService.generateCropRecommendation(
                latestSoilData,
                weather,
                recentFertilizer,
                field.getAreaInAcres(),
                stalenessTier.name()
        );

        // 7. Save result as new CropRecommendation linked to SoilData
        CropRecommendation recommendation = CropRecommendation.builder()
                .field(field)
                .soilData(latestSoilData)
                .recommendedCrop(geminiResult.getRecommendedCrop())
                .confidenceScore(geminiResult.getConfidenceScore())
                .reasoning(geminiResult.getReasoning())
                .fertilizerAdvice(geminiResult.getFertilizerAdvice())
                .recommendationDate(LocalDateTime.now())
                .build();

        CropRecommendation saved = cropRecommendationRepository.save(recommendation);

        // 8. Return response DTO with dataFreshnessWarning
        CropRecommendationResponseDto responseDto = cropRecommendationMapper.toResponseDto(saved);
        responseDto.setDataFreshnessWarning(determineFreshnessWarning(stalenessTier));
        return responseDto;
    }

    @Override
    public CropRecommendationResponseDto generateCropRecommendationForField(Long fieldId) {
        return generateCropRecommendationForField(fieldId, null);
    }

    private String determineFreshnessWarning(SoilStalenessTier tier) {
        if (tier == null) {
            return null;
        }
        return switch (tier) {
            case FRESH -> null;
            case MODERATE_STALE -> "The soil health data is between 6 and 24 months old. We suggest uploading a fresh soil card soon for more accurate results.";
            case HIGH_STALE -> "The soil health data is between 24 and 36 months old. This recommendation may be significantly outdated or deprecated. A fresh soil card upload is strongly recommended before relying on this advice.";
            case CRITICAL_STALE -> "Soil data is over 36 months old and critically stale.";
        };
    }

    private void checkFieldAccess(Field field, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            return;
        }
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authenticatedUserId));

        boolean isAdmin = user.getRoles() != null && user.getRoles().contains(Role.ADMIN);
        if (!isAdmin) {
            if (user.getFarmer() == null || field.getFarmer() == null ||
                    !field.getFarmer().getId().equals(user.getFarmer().getId())) {
                throw new AccessDeniedException("You do not have permission to access this resource");
            }
        }
    }
}
