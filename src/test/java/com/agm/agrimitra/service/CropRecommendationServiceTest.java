package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.dto.CropRecommendationResult;
import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.entity.Address;
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
import com.agm.agrimitra.service.impl.CropRecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CropRecommendationServiceTest {

    @Mock
    private CropRecommendationRepository cropRecommendationRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private CropRecommendationMapper cropRecommendationMapper;

    @Mock
    private SoilDataService soilDataService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private FertilizerUsageRepository fertilizerUsageRepository;

    @Mock
    private GeminiService geminiService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CropRecommendationServiceImpl cropRecommendationService;

    private User ownerUser;
    private Farmer ownerFarmer;
    private Field field;
    private SoilData soilData;
    private CropRecommendationResult geminiResult;
    private CropRecommendation savedEntity;
    private CropRecommendationResponseDto mappedDto;

    @BeforeEach
    void setUp() {
        ownerFarmer = Farmer.builder()
                .id(1L)
                .name("Ramesh Kumar")
                .location(Address.builder().village("Shirwal").district("Pune").state("Maharashtra").build())
                .build();

        ownerUser = User.builder()
                .id(10L)
                .farmer(ownerFarmer)
                .roles(Set.of(Role.FARMER))
                .build();

        field = Field.builder()
                .id(100L)
                .fieldName("North Parcel")
                .areaInAcres(5.0)
                .farmer(ownerFarmer)
                .build();

        soilData = SoilData.builder()
                .id(200L)
                .field(field)
                .nitrogenLevel(280.0)
                .phosphorusLevel(25.0)
                .potassiumLevel(180.0)
                .phLevel(6.8)
                .testedDate(LocalDate.now().minusMonths(2))
                .build();

        geminiResult = CropRecommendationResult.builder()
                .recommendedCrop("Wheat")
                .confidenceScore(0.88)
                .reasoning("Ideal NPK levels and mild winter temperatures.")
                .fertilizerAdvice("Apply 50 kg Urea at crown root initiation stage.")
                .build();

        savedEntity = CropRecommendation.builder()
                .id(500L)
                .field(field)
                .soilData(soilData)
                .recommendedCrop("Wheat")
                .confidenceScore(0.88)
                .reasoning("Ideal NPK levels and mild winter temperatures.")
                .fertilizerAdvice("Apply 50 kg Urea at crown root initiation stage.")
                .recommendationDate(LocalDateTime.now())
                .build();

        mappedDto = CropRecommendationResponseDto.builder()
                .id(500L)
                .fieldId(100L)
                .soilDataId(200L)
                .recommendedCrop("Wheat")
                .confidenceScore(0.88)
                .reasoning("Ideal NPK levels and mild winter temperatures.")
                .fertilizerAdvice("Apply 50 kg Urea at crown root initiation stage.")
                .recommendationDate(LocalDateTime.now())
                .build();
    }

    @Test
    void testGenerateCropRecommendation_Success_FreshSoil() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(soilDataService.getLatestSoilDataForField(100L)).thenReturn(Optional.of(soilData));
        when(soilDataService.calculateStalenessTier(soilData)).thenReturn(SoilStalenessTier.FRESH);

        WeatherDataDto weather = WeatherDataDto.builder()
                .temperature(24.5)
                .humidity(55)
                .weatherCondition("clear sky")
                .windSpeed(3.2)
                .fetchedAt(LocalDateTime.now())
                .build();
        when(weatherService.getCurrentWeather("Pune", "Maharashtra")).thenReturn(Optional.of(weather));

        when(fertilizerUsageRepository.findByFieldIdOrderByApplicationDateDesc(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(geminiService.generateCropRecommendation(eq(soilData), eq(weather), anyList(), eq(5.0), eq("FRESH")))
                .thenReturn(geminiResult);
        when(cropRecommendationRepository.save(any(CropRecommendation.class))).thenReturn(savedEntity);
        when(cropRecommendationMapper.toResponseDto(savedEntity)).thenReturn(mappedDto);

        CropRecommendationResponseDto result = cropRecommendationService.generateCropRecommendationForField(100L, 10L);

        assertNotNull(result);
        assertEquals("Wheat", result.getRecommendedCrop());
        assertEquals(0.88, result.getConfidenceScore());
        assertEquals("Ideal NPK levels and mild winter temperatures.", result.getReasoning());
        assertNull(result.getDataFreshnessWarning(), "Fresh soil data should have null dataFreshnessWarning");
        verify(geminiService).generateCropRecommendation(eq(soilData), eq(weather), anyList(), eq(5.0), eq("FRESH"));
        verify(cropRecommendationRepository).save(any(CropRecommendation.class));
    }

    @Test
    void testGenerateCropRecommendation_ModerateStale() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(soilDataService.getLatestSoilDataForField(100L)).thenReturn(Optional.of(soilData));
        when(soilDataService.calculateStalenessTier(soilData)).thenReturn(SoilStalenessTier.MODERATE_STALE);

        when(weatherService.getCurrentWeather("Pune", "Maharashtra")).thenReturn(Optional.empty());
        when(fertilizerUsageRepository.findByFieldIdOrderByApplicationDateDesc(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(geminiService.generateCropRecommendation(eq(soilData), any(), anyList(), eq(5.0), eq("MODERATE_STALE")))
                .thenReturn(geminiResult);
        when(cropRecommendationRepository.save(any(CropRecommendation.class))).thenReturn(savedEntity);
        when(cropRecommendationMapper.toResponseDto(savedEntity)).thenReturn(mappedDto);

        CropRecommendationResponseDto result = cropRecommendationService.generateCropRecommendationForField(100L, 10L);

        assertNotNull(result);
        assertNotNull(result.getDataFreshnessWarning());
        assertTrue(result.getDataFreshnessWarning().contains("6 and 24 months old"));
    }

    @Test
    void testGenerateCropRecommendation_HighStale() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(soilDataService.getLatestSoilDataForField(100L)).thenReturn(Optional.of(soilData));
        when(soilDataService.calculateStalenessTier(soilData)).thenReturn(SoilStalenessTier.HIGH_STALE);

        when(weatherService.getCurrentWeather("Pune", "Maharashtra")).thenReturn(Optional.empty());
        when(fertilizerUsageRepository.findByFieldIdOrderByApplicationDateDesc(eq(100L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        when(geminiService.generateCropRecommendation(eq(soilData), any(), anyList(), eq(5.0), eq("HIGH_STALE")))
                .thenReturn(geminiResult);
        when(cropRecommendationRepository.save(any(CropRecommendation.class))).thenReturn(savedEntity);
        when(cropRecommendationMapper.toResponseDto(savedEntity)).thenReturn(mappedDto);

        CropRecommendationResponseDto result = cropRecommendationService.generateCropRecommendationForField(100L, 10L);

        assertNotNull(result);
        assertNotNull(result.getDataFreshnessWarning());
        assertTrue(result.getDataFreshnessWarning().contains("outdated or deprecated"));
        verify(geminiService).generateCropRecommendation(eq(soilData), any(), anyList(), eq(5.0), eq("HIGH_STALE"));
    }

    @Test
    void testGenerateCropRecommendation_CriticalStale_Throws422() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(soilDataService.getLatestSoilDataForField(100L)).thenReturn(Optional.of(soilData));
        when(soilDataService.calculateStalenessTier(soilData)).thenReturn(SoilStalenessTier.CRITICAL_STALE);

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                cropRecommendationService.generateCropRecommendationForField(100L, 10L));

        assertTrue(exception.getMessage().contains("36 months old"));
        // Confirm Gemini is NEVER called for CRITICAL_STALE
        verify(geminiService, never()).generateCropRecommendation(any(), any(), any(), anyDouble(), anyString());
        verify(cropRecommendationRepository, never()).save(any());
    }

    @Test
    void testGenerateCropRecommendation_NoSoilData_Throws422() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(10L)).thenReturn(Optional.of(ownerUser));
        when(soilDataService.getLatestSoilDataForField(100L)).thenReturn(Optional.empty());

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class, () ->
                cropRecommendationService.generateCropRecommendationForField(100L, 10L));

        assertTrue(exception.getMessage().contains("soil card upload is required first"));
        verify(geminiService, never()).generateCropRecommendation(any(), any(), any(), anyDouble(), anyString());
    }

    @Test
    void testGenerateCropRecommendation_FieldNotFound() {
        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                cropRecommendationService.generateCropRecommendationForField(999L, 10L));
    }

    @Test
    void testGenerateCropRecommendation_AccessDenied() {
        User otherUser = User.builder()
                .id(20L)
                .farmer(Farmer.builder().id(2L).name("Other Farmer").build())
                .roles(Set.of(Role.FARMER))
                .build();

        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(20L)).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                cropRecommendationService.generateCropRecommendationForField(100L, 20L));
    }
}
