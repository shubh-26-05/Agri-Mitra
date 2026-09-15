package com.agm.agrimitra.mapper;

import com.agm.agrimitra.dto.AddressDto;
import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import com.agm.agrimitra.entity.Address;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.entity.User;
import org.springframework.stereotype.Component;

@Component
public class FarmerMapper {

    public Farmer toEntity(FarmerRequestDto dto, User user) {
        if (dto == null) {
            return null;
        }

        Address address = null;
        if (dto.getLocation() != null) {
            address = Address.builder()
                    .village(dto.getLocation().getVillage())
                    .district(dto.getLocation().getDistrict())
                    .state(dto.getLocation().getState())
                    .build();
        }

        return Farmer.builder()
                .user(user)
                .name(dto.getName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .location(address)
                .build();
    }

    public Farmer toEntity(FarmerRequestDto dto) {
        return toEntity(dto, null);
    }

    public FarmerResponseDto toResponseDto(Farmer entity) {
        if (entity == null) {
            return null;
        }

        AddressDto addressDto = null;
        if (entity.getLocation() != null) {
            addressDto = AddressDto.builder()
                    .village(entity.getLocation().getVillage())
                    .district(entity.getLocation().getDistrict())
                    .state(entity.getLocation().getState())
                    .build();
        }

        Long userId = entity.getUser() != null ? entity.getUser().getId() : null;

        return FarmerResponseDto.builder()
                .id(entity.getId())
                .userId(userId)
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .location(addressDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(FarmerRequestDto dto, Farmer entity) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setName(dto.getName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setEmail(dto.getEmail());

        if (dto.getLocation() != null) {
            if (entity.getLocation() == null) {
                entity.setLocation(new Address());
            }
            entity.getLocation().setVillage(dto.getLocation().getVillage());
            entity.getLocation().setDistrict(dto.getLocation().getDistrict());
            entity.getLocation().setState(dto.getLocation().getState());
        }
    }
}
