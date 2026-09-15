package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FertilizerUsageMapper;
import com.agm.agrimitra.repository.FertilizerUsageRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.service.FertilizerUsageService;
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
public class FertilizerUsageServiceImpl implements FertilizerUsageService {

    private final FertilizerUsageRepository fertilizerUsageRepository;
    private final FieldRepository fieldRepository;
    private final FertilizerUsageMapper fertilizerUsageMapper;

    @Override
    public FertilizerUsageResponseDto createFertilizerUsage(Long fieldId, FertilizerUsageRequestDto requestDto) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", "id", fieldId));

        FertilizerUsage fertilizerUsage = fertilizerUsageMapper.toEntity(requestDto, field);
        FertilizerUsage saved = fertilizerUsageRepository.save(fertilizerUsage);
        return fertilizerUsageMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, Pageable pageable) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResourceNotFoundException("Field", "id", fieldId);
        }

        return fertilizerUsageRepository.findByFieldId(fieldId, pageable)
                .map(fertilizerUsageMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FertilizerUsageResponseDto> getFertilizerUsageByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir) {
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "applicationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));
        return getFertilizerUsageByFieldId(fieldId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FertilizerUsageResponseDto getFertilizerUsageById(Long id) {
        FertilizerUsage usage = fertilizerUsageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FertilizerUsage", "id", id));
        return fertilizerUsageMapper.toResponseDto(usage);
    }

    @Override
    public void deleteFertilizerUsage(Long id) {
        if (!fertilizerUsageRepository.existsById(id)) {
            throw new ResourceNotFoundException("FertilizerUsage", "id", id);
        }
        fertilizerUsageRepository.deleteById(id);
    }
}
