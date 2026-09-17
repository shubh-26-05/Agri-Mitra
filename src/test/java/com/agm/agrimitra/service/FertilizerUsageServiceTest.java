package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.Role;
import com.agm.agrimitra.entity.User;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FertilizerUsageMapper;
import com.agm.agrimitra.repository.FertilizerUsageRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.UserRepository;
import com.agm.agrimitra.service.impl.FertilizerUsageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FertilizerUsageServiceTest {

    @Mock
    private FertilizerUsageRepository fertilizerUsageRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private FertilizerUsageMapper fertilizerUsageMapper = new FertilizerUsageMapper();

    @InjectMocks
    private FertilizerUsageServiceImpl fertilizerUsageService;

    private User ownerUser;
    private User otherUser;
    private Farmer ownerFarmer;
    private Field field;
    private FertilizerUsage fertilizerUsage;
    private FertilizerUsageRequestDto requestDto;

    @BeforeEach
    void setUp() {
        ownerFarmer = Farmer.builder()
                .id(10L)
                .name("Ramesh Kumar")
                .build();

        ownerUser = User.builder()
                .id(1L)
                .email("ramesh@example.com")
                .roles(Set.of(Role.FARMER))
                .farmer(ownerFarmer)
                .build();

        Farmer otherFarmer = Farmer.builder()
                .id(20L)
                .name("Suresh Patel")
                .build();

        otherUser = User.builder()
                .id(2L)
                .email("suresh@example.com")
                .roles(Set.of(Role.FARMER))
                .farmer(otherFarmer)
                .build();

        field = Field.builder()
                .id(100L)
                .fieldName("North Field")
                .areaInAcres(5.0)
                .farmer(ownerFarmer)
                .build();

        fertilizerUsage = FertilizerUsage.builder()
                .id(500L)
                .field(field)
                .fertilizerType("Urea")
                .quantityUsed(50.0)
                .quantityUnit("kg")
                .applicationDate(LocalDate.now().minusDays(2))
                .createdAt(LocalDateTime.now())
                .build();

        requestDto = FertilizerUsageRequestDto.builder()
                .fertilizerType("Urea")
                .quantityUsed(50.0)
                .quantityUnit("kg")
                .applicationDate(LocalDate.now().minusDays(2))
                .build();
    }

    @Test
    @DisplayName("Should successfully create fertilizer usage for field owner")
    void testCreateFertilizerUsage_Success() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(fertilizerUsageRepository.save(any(FertilizerUsage.class))).thenReturn(fertilizerUsage);

        FertilizerUsageResponseDto response = fertilizerUsageService.createFertilizerUsage(100L, requestDto, 1L);

        assertNotNull(response);
        assertEquals("Urea", response.getFertilizerType());
        assertEquals(50.0, response.getQuantityUsed());
        assertEquals("kg", response.getQuantityUnit());
        assertEquals(100L, response.getFieldId());
        verify(fertilizerUsageRepository, times(1)).save(any(FertilizerUsage.class));
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when non-owner non-admin creates fertilizer usage")
    void testCreateFertilizerUsage_AccessDenied() {
        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                fertilizerUsageService.createFertilizerUsage(100L, requestDto, 2L));
        verify(fertilizerUsageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when field does not exist")
    void testCreateFertilizerUsage_FieldNotFound() {
        when(fieldRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                fertilizerUsageService.createFertilizerUsage(999L, requestDto, 1L));
    }

    @Test
    @DisplayName("Should return paginated fertilizer usage history for field owner")
    void testGetFertilizerUsageHistory_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<FertilizerUsage> page = new PageImpl<>(List.of(fertilizerUsage), pageable, 1);

        when(fieldRepository.findById(100L)).thenReturn(Optional.of(field));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));
        when(fertilizerUsageRepository.findByFieldId(100L, pageable)).thenReturn(page);

        Page<FertilizerUsageResponseDto> result = fertilizerUsageService.getFertilizerUsageHistory(100L, pageable, 1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Urea", result.getContent().get(0).getFertilizerType());
    }

    @Test
    @DisplayName("Should successfully delete fertilizer usage for owner")
    void testDeleteFertilizerUsage_Success() {
        when(fertilizerUsageRepository.findById(500L)).thenReturn(Optional.of(fertilizerUsage));
        when(userRepository.findById(1L)).thenReturn(Optional.of(ownerUser));

        fertilizerUsageService.deleteFertilizerUsage(500L, 1L);

        verify(fertilizerUsageRepository, times(1)).delete(fertilizerUsage);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when non-owner non-admin deletes fertilizer usage")
    void testDeleteFertilizerUsage_AccessDenied() {
        when(fertilizerUsageRepository.findById(500L)).thenReturn(Optional.of(fertilizerUsage));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () ->
                fertilizerUsageService.deleteFertilizerUsage(500L, 2L));
        verify(fertilizerUsageRepository, never()).delete(any());
    }
}
