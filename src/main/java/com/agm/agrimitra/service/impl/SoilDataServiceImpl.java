package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.entity.SoilStalenessTier;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    @Override
    @Transactional(readOnly = true)
    public long calculateSoilDataAgeInMonths(SoilData soilData) {
        if (soilData == null) {
            return Long.MAX_VALUE;
        }
        LocalDate date = getEffectiveDate(soilData);
        if (date.equals(LocalDate.MIN)) {
            return Long.MAX_VALUE;
        }
        LocalDate now = LocalDate.now();
        if (date.isAfter(now)) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(date, now);
    }

    @Override
    @Transactional(readOnly = true)
    public SoilStalenessTier calculateStalenessTier(SoilData soilData) {
        long months = calculateSoilDataAgeInMonths(soilData);
        if (months < 6) {
            return SoilStalenessTier.FRESH;
        } else if (months < 24) {
            return SoilStalenessTier.MODERATE_STALE;
        } else if (months <= 36) {
            return SoilStalenessTier.HIGH_STALE;
        } else {
            return SoilStalenessTier.CRITICAL_STALE;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SoilData> getLatestSoilDataForField(Long fieldId) {
        List<SoilData> records = soilDataRepository.findByFieldId(fieldId);
        if (records.isEmpty()) {
            return Optional.empty();
        }
        return records.stream()
                .max(Comparator.comparing(this::getEffectiveDate)
                        .thenComparing(s -> s.getCreatedAt() != null ? s.getCreatedAt() : LocalDateTime.MIN));
    }

    private LocalDate getEffectiveDate(SoilData soilData) {
        if (soilData.getTestedDate() != null) {
            return soilData.getTestedDate();
        }
        if (soilData.getCreatedAt() != null) {
            return soilData.getCreatedAt().toLocalDate();
        }
        return LocalDate.MIN;
    }
}
