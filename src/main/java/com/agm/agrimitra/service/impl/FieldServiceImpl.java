package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.FieldRequestDto;
import com.agm.agrimitra.dto.FieldResponseDto;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FieldMapper;
import com.agm.agrimitra.repository.FarmerRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FieldServiceImpl implements FieldService {

    private final FieldRepository fieldRepository;
    private final FarmerRepository farmerRepository;
    private final FieldMapper fieldMapper;

    @Override
    public FieldResponseDto createField(Long farmerId, FieldRequestDto requestDto) {
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "id", farmerId));

        Field field = fieldMapper.toEntity(requestDto, farmer);
        Field savedField = fieldRepository.save(field);
        return fieldMapper.toResponseDto(savedField);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldResponseDto> getFieldsByFarmerId(Long farmerId) {
        if (!farmerRepository.existsById(farmerId)) {
            throw new ResourceNotFoundException("Farmer", "id", farmerId);
        }

        return fieldRepository.findByFarmerId(farmerId)
                .stream()
                .map(fieldMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FieldResponseDto getFieldById(Long id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", id));
        return fieldMapper.toResponseDto(field);
    }

    @Override
    public FieldResponseDto updateField(Long id, FieldRequestDto requestDto) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", id));

        fieldMapper.updateEntityFromDto(requestDto, field);
        Field updatedField = fieldRepository.save(field);
        return fieldMapper.toResponseDto(updatedField);
    }

    @Override
    public void deleteField(Long id) {
        if (!fieldRepository.existsById(id)) {
            throw new ResourceNotFoundException("Field", "id", id);
        }
        fieldRepository.deleteById(id);
    }
}
