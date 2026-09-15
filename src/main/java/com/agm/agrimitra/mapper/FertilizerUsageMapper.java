package com.agm.agrimitra.mapper;

import com.agm.agrimitra.dto.FertilizerUsageRequestDto;
import com.agm.agrimitra.dto.FertilizerUsageResponseDto;
import com.agm.agrimitra.entity.Field;
import com.agm.agrimitra.entity.FertilizerUsage;
import org.springframework.stereotype.Component;

@Component
public class FertilizerUsageMapper {

    public FertilizerUsage toEntity(FertilizerUsageRequestDto dto, Field field) {
        if (dto == null) {
            return null;
        }

        return FertilizerUsage.builder()
                .field(field)
                .fertilizerType(dto.getFertilizerType())
                .quantityUsed(dto.getQuantityUsed())
                .quantityUnit(dto.getQuantityUnit())
                .applicationDate(dto.getApplicationDate())
                .build();
    }

    public FertilizerUsageResponseDto toResponseDto(FertilizerUsage entity) {
        if (entity == null) {
            return null;
        }

        Long fieldId = entity.getField() != null ? entity.getField().getId() : null;

        return FertilizerUsageResponseDto.builder()
                .id(entity.getId())
                .fieldId(fieldId)
                .fertilizerType(entity.getFertilizerType())
                .quantityUsed(entity.getQuantityUsed())
                .quantityUnit(entity.getQuantityUnit())
                .applicationDate(entity.getApplicationDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
