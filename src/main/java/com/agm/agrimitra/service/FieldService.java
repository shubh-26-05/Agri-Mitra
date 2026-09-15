package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FieldRequestDto;
import com.agm.agrimitra.dto.FieldResponseDto;

import java.util.List;

public interface FieldService {

    FieldResponseDto createField(Long farmerId, FieldRequestDto requestDto);

    List<FieldResponseDto> getFieldsByFarmerId(Long farmerId);

    FieldResponseDto getFieldById(Long id);

    FieldResponseDto updateField(Long id, FieldRequestDto requestDto);

    void deleteField(Long id);
}
