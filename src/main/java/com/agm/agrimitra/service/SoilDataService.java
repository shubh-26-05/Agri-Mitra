package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.entity.SoilStalenessTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SoilDataService {

    SoilDataResponseDto createSoilData(Long fieldId, SoilDataRequestDto requestDto);

    Page<SoilDataResponseDto> getSoilDataByFieldId(Long fieldId, Pageable pageable);

    Page<SoilDataResponseDto> getSoilDataByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir);

    SoilDataResponseDto getSoilDataById(Long id);

    void deleteSoilData(Long id);

    long calculateSoilDataAgeInMonths(SoilData soilData);

    SoilStalenessTier calculateStalenessTier(SoilData soilData);

    Optional<SoilData> getLatestSoilDataForField(Long fieldId);
}
