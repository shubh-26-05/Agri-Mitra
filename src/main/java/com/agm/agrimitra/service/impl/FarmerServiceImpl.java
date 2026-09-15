package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import com.agm.agrimitra.entity.Farmer;
import com.agm.agrimitra.exception.ResourceNotFoundException;
import com.agm.agrimitra.mapper.FarmerMapper;
import com.agm.agrimitra.repository.FarmerRepository;
import com.agm.agrimitra.service.FarmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmerServiceImpl implements FarmerService {

    private final FarmerRepository farmerRepository;
    private final FarmerMapper farmerMapper;

    @Override
    public FarmerResponseDto createFarmer(FarmerRequestDto requestDto) {
        if (farmerRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("Farmer with phone number " + requestDto.getPhoneNumber() + " already exists");
        }

        Farmer farmer = farmerMapper.toEntity(requestDto);
        Farmer savedFarmer = farmerRepository.save(farmer);
        return farmerMapper.toResponseDto(savedFarmer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmerResponseDto> getAllFarmers() {
        return farmerRepository.findAll()
                .stream()
                .map(farmerMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FarmerResponseDto getFarmerById(Long id) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "id", id));
        return farmerMapper.toResponseDto(farmer);
    }

    @Override
    public FarmerResponseDto updateFarmer(Long id, FarmerRequestDto requestDto) {
        Farmer farmer = farmerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer", "id", id));

        if (!farmer.getPhoneNumber().equals(requestDto.getPhoneNumber())
                && farmerRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("Farmer with phone number " + requestDto.getPhoneNumber() + " already exists");
        }

        farmerMapper.updateEntityFromDto(requestDto, farmer);
        Farmer updatedFarmer = farmerRepository.save(farmer);
        return farmerMapper.toResponseDto(updatedFarmer);
    }

    @Override
    public void deleteFarmer(Long id) {
        if (!farmerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Farmer", "id", id);
        }
        farmerRepository.deleteById(id);
    }
}
