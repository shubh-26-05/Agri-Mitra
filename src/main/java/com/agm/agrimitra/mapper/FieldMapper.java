package com.agm.agrimitra.mapper;

import com.agm.agrimitra.dto.FieldRequestDto;
import com.agm.agrimitra.dto.FieldResponseDto;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.Field;
import org.springframework.stereotype.Component;

@Component
public class FieldMapper {

    public Field toEntity(FieldRequestDto dto, Farmer farmer) {
        if (dto == null) {
            return null;
        }

        return Field.builder()
                .fieldName(dto.getFieldName())
                .areaInAcres(dto.getAreaInAcres())
                .farmer(farmer)
                .build();
    }

    public FieldResponseDto toResponseDto(Field entity) {
        if (entity == null) {
            return null;
        }

        Long farmerId = entity.getFarmer() != null ? entity.getFarmer().getId() : null;

        return FieldResponseDto.builder()
                .id(entity.getId())
                .fieldName(entity.getFieldName())
                .areaInAcres(entity.getAreaInAcres())
                .farmerId(farmerId)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(FieldRequestDto dto, Field entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setFieldName(dto.getFieldName());
        entity.setAreaInAcres(dto.getAreaInAcres());
    }
}
