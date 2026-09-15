package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.CropRecommendationResponseDto;
import com.agm.agrimitra.entity.CropRecommendation;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.CropRecommendationMapper;
import com.agm.agrimitra.repository.CropRecommendationRepository;
import com.agm.agrimitra.repository.FieldRepository;
import com.agm.agrimitra.service.CropRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CropRecommendationServiceImpl implements CropRecommendationService {

    private final CropRecommendationRepository cropRecommendationRepository;
    private final FieldRepository fieldRepository;
    private final CropRecommendationMapper cropRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CropRecommendationResponseDto> getRecommendationsByFieldId(Long fieldId) {
        if (!fieldRepository.existsById(fieldId)) {
            throw new ResourceNotFoundException("Field", "id", fieldId);
        }

        return cropRecommendationRepository.findByFieldIdOrderByRecommendationDateDesc(fieldId)
                .stream()
                .map(cropRecommendationMapper::toResponseDto)
                .toList();
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
