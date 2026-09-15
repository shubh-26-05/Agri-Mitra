package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.SoilDataMapper;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.repository.SoilDataRepository;
import com.agm.agrimitra.service.SoilDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SoilDataServiceImpl implements SoilDataService {

    private final SoilDataRepository soilDataRepository;
    private final FieldRepository fieldRepository;
    private final SoilDataMapper soilDataMapper;

    @Override
    public SoilDataResponseDto createSoilData(Long fieldId, SoilDataRequestDto requestDto) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        SoilData soilData = soilDataMapper.toEntity(requestDto, field);
        SoilData savedSoilData = soilDataRepository.save(soilData);
        return soilDataMapper.toResponseDto(savedSoilData);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoilDataResponseDto> getSoilDataByFieldId(Long fieldId, Pageable pageable) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResourceNotFoundException("Field", "id", fieldId);
        }

        return soilDataRepository.findByFieldId(fieldId, pageable)
                .map(soilDataMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SoilDataResponseDto> getSoilDataByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir) {
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "testedDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));
        return getSoilDataByFieldId(fieldId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public SoilDataResponseDto getSoilDataById(Long id) {
        SoilData soilData = soilDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SoilData", "id", id));
        return soilDataMapper.toResponseDto(soilData);
    }

    @Override
    public void deleteSoilData(Long id) {
        if (!soilDataRepository.existsById(id)) {
            throw new ResourceNotFoundException("SoilData", "id", id);
        }
        soilDataRepository.deleteById(id);
    }
}
