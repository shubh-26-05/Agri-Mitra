package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;

import java.util.List;

public interface SoilDataService {

    SoilDataResponseDto createSoilData(Long fieldId, SoilDataRequestDto requestDto);

    List<SoilDataResponseDto> getSoilDataByFieldId(Long fieldId);

    SoilDataResponseDto getSoilDataById(Long id);

    void deleteSoilData(Long id);
}
