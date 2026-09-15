package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;

import java.util.List;

public interface FarmerService {

    FarmerResponseDto createFarmer(FarmerRequestDto requestDto);

    List<FarmerResponseDto> getAllFarmers();

    FarmerResponseDto getFarmerById(Long id);

    FarmerResponseDto updateFarmer(Long id, FarmerRequestDto requestDto);

    void deleteFarmer(Long id);
}
