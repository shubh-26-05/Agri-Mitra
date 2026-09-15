package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FieldRequestDto;
import com.agm.agrimitra.dto.FieldResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FieldService {

    FieldResponseDto createField(Long farmerId, FieldRequestDto requestDto);

    Page<FieldResponseDto> getFieldsByFarmerId(Long farmerId, Pageable pageable);

    Page<FieldResponseDto> getFieldsByFarmerId(Long farmerId, int page, int size, String sortBy, String sortDir);

    FieldResponseDto getFieldById(Long id);

    FieldResponseDto updateField(Long id, FieldRequestDto requestDto);

    void deleteField(Long id);
}
