package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FarmerService {

    FarmerResponseDto createFarmer(FarmerRequestDto requestDto);

    Page<FarmerResponseDto> getAllFarmers(Pageable pageable);

    Page<FarmerResponseDto> getAllFarmers(int page, int size, String sortBy, String sortDir);

    FarmerResponseDto getFarmerById(Long id);

    FarmerResponseDto updateFarmer(Long id, FarmerRequestDto requestDto);

    void deleteFarmer(Long id);
}
