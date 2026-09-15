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
                .electricalConductivity(dto.getElectricalConductivity())
                .organicCarbon(dto.getOrganicCarbon())
                .moistureLevel(dto.getMoistureLevel())
                .sulphurLevel(dto.getSulphurLevel())
                .zincLevel(dto.getZincLevel())
                .ironLevel(dto.getIronLevel())
                .copperLevel(dto.getCopperLevel())
                .manganeseLevel(dto.getManganeseLevel())
                .boronLevel(dto.getBoronLevel())
                .testedDate(dto.getTestedDate())
                .imageUrl(dto.getImageUrl())
                .dataSource(dto.getDataSource())
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
                .electricalConductivity(entity.getElectricalConductivity())
                .organicCarbon(entity.getOrganicCarbon())
                .moistureLevel(entity.getMoistureLevel())
                .sulphurLevel(entity.getSulphurLevel())
                .zincLevel(entity.getZincLevel())
                .ironLevel(entity.getIronLevel())
                .copperLevel(entity.getCopperLevel())
                .manganeseLevel(entity.getManganeseLevel())
                .boronLevel(entity.getBoronLevel())
                .testedDate(entity.getTestedDate())
                .imageUrl(entity.getImageUrl())
                .dataSource(entity.getDataSource())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
