package com.agm.agrimitra.service;

import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.entity.SoilStalenessTier;
import com.agm.agrimitra.mapper.SoilDataMapper;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.SoilDataRepository;
import com.agm.agrimitra.service.impl.SoilDataServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoilDataStalenessTest {

    @Mock
    private SoilDataRepository soilDataRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private SoilDataMapper soilDataMapper;

    @InjectMocks
    private SoilDataServiceImpl soilDataService;

    private Field field;

    @BeforeEach
    void setUp() {
        field = Field.builder().id(1L).fieldName("Main Field").build();
    }

    @Test
    void testStalenessTier_Fresh() {
        SoilData soilData = SoilData.builder()
                .field(field)
                .testedDate(LocalDate.now().minusMonths(3))
                .build();

        assertEquals(SoilStalenessTier.FRESH, soilDataService.calculateStalenessTier(soilData));
        assertEquals(3, soilDataService.calculateSoilDataAgeInMonths(soilData));
    }

    @Test
    void testStalenessTier_ModerateStale() {
        SoilData soilData = SoilData.builder()
                .field(field)
                .testedDate(LocalDate.now().minusMonths(15))
                .build();

        assertEquals(SoilStalenessTier.MODERATE_STALE, soilDataService.calculateStalenessTier(soilData));
    }

    @Test
    void testStalenessTier_HighStale() {
        SoilData soilData = SoilData.builder()
                .field(field)
                .testedDate(LocalDate.now().minusMonths(28))
                .build();

        assertEquals(SoilStalenessTier.HIGH_STALE, soilDataService.calculateStalenessTier(soilData));
    }

    @Test
    void testStalenessTier_CriticalStale() {
        SoilData soilData = SoilData.builder()
                .field(field)
                .testedDate(LocalDate.now().minusMonths(40))
                .build();

        assertEquals(SoilStalenessTier.CRITICAL_STALE, soilDataService.calculateStalenessTier(soilData));
    }

    @Test
    void testStalenessTier_FallbackToCreatedAtWhenTestedDateNull() {
        SoilData soilData = SoilData.builder()
                .field(field)
                .testedDate(null)
                .createdAt(LocalDateTime.now().minusMonths(2))
                .build();

        assertEquals(SoilStalenessTier.FRESH, soilDataService.calculateStalenessTier(soilData));
    }

    @Test
    void testGetLatestSoilDataForField_PicksMostRecent() {
        SoilData oldSoil = SoilData.builder()
                .id(1L)
                .field(field)
                .testedDate(LocalDate.now().minusMonths(20))
                .build();
        SoilData newSoil = SoilData.builder()
                .id(2L)
                .field(field)
                .testedDate(LocalDate.now().minusMonths(2))
                .build();

        when(soilDataRepository.findByFieldId(1L)).thenReturn(List.of(oldSoil, newSoil));

        Optional<SoilData> result = soilDataService.getLatestSoilDataForField(1L);

        assertTrue(result.isPresent());
        assertEquals(2L, result.get().getId());
    }
}
