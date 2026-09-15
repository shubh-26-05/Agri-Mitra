package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FertilizerUsageService {

    FertilizerUsageResponseDto createFertilizerUsage(Long fieldId, FertilizerUsageRequestDto requestDto);

    Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, Pageable pageable);

    Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir);

    FertilizerUsageResponseDto getFertilizerUsageById(Long id);

    void deleteFertilizerUsage(Long id);
}
