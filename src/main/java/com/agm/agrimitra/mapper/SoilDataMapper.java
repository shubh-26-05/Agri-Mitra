package com.agm.agrimitra.mapper;

import com.agm.agrimitra.dto.SoilDataRequestDto;
import com.agm.agrimitra.dto.SoilDataResponseDto;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.SoilData;
import org.springframework.stereotype.Component;

@Component
public class SoilDataMapper {

    public SoilData toEntity(SoilDataRequestDto dto, Field field) {
        if (dto == null) {
            return null;
        }

        return SoilData.builder()
                .field(field)
                .nitrogenLevel(dto.getNitrogenLevel())
                .phosphorusLevel(dto.getPhosphorusLevel())
                .potassiumLevel(dto.getPotassiumLevel())
                .phLevel(dto.getPhLevel())
                .moistureLevel(dto.getMoistureLevel())
                .testedDate(dto.getTestedDate())
                .build();
    }

    public SoilDataResponseDto toResponseDto(SoilData entity) {
        if (entity == null) {
            return null;
        }

        Long fieldId = entity.getField() != null ? entity.getField().getId() : null;

        return SoilDataResponseDto.builder()
                .id(entity.getId())
                .fieldId(fieldId)
                .nitrogenLevel(entity.getNitrogenLevel())
                .phosphorusLevel(entity.getPhosphorusLevel())
                .potassiumLevel(entity.getPotassiumLevel())
                .phLevel(entity.getPhLevel())
                .moistureLevel(entity.getMoistureLevel())
                .testedDate(entity.getTestedDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
