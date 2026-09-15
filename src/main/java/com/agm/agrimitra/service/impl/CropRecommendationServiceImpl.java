package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.entity.CropRecommendation;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.CropRecommendationMapper;
import com.agm.agrimitra.repository.CropRecommendationRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.service.CropRecommendationService;
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
public class CropRecommendationServiceImpl implements CropRecommendationService {

    private final CropRecommendationRepository cropRecommendationRepository;
    private final FieldRepository fieldRepository;
    private final CropRecommendationMapper cropRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, Pageable pageable) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResourceNotFoundException("Field", "id", fieldId);
        }

        return cropRecommendationRepository.findByFieldId(fieldId, pageable)
                .map(cropRecommendationMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId, int page, int size, String sortBy, String sortDir) {
        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "recommendationDate";
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortField));
        return getRecommendationsByFieldId(fieldId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public CropRecommendationResponseDto getRecommendationById(Long id) {
        CropRecommendation recommendation = cropRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CropRecommendation", "id", id));
        return cropRecommendationMapper.toResponseDto(recommendation);
    }

    @Override
    public void deleteRecommendation(Long id) {
        if (!cropRecommendationRepository.existsById(id)) {
            throw new ResourceNotFoundException("CropRecommendation", "id", id);
        }
        cropRecommendationRepository.deleteById(id);
    }
}
