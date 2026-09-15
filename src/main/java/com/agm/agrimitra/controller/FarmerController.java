package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import com.agm.agrimitra.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
@Tag(name = "Farmer Controller", description = "Endpoints for managing farmers")
public class FarmerController {

    private final FarmerService farmerService;

    @PostMapping
    @Operation(summary = "Create a new farmer")
    public ResponseEntity<FarmerResponseDto> createFarmer(@Valid @RequestBody FarmerRequestDto requestDto) {
        FarmerResponseDto createdFarmer = farmerService.createFarmer(requestDto);
        return new ResponseEntity<>(createdFarmer, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all farmers")
    public ResponseEntity<List<FarmerResponseDto>> getAllFarmers() {
        List<FarmerResponseDto> farmers = farmerService.getAllFarmers();
        return ResponseEntity.ok(farmers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get farmer by ID")
    public ResponseEntity<FarmerResponseDto> getFarmerById(@PathVariable Long id) {
        FarmerResponseDto farmer = farmerService.getFarmerById(id);
        return ResponseEntity.ok(farmer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing farmer")
    public ResponseEntity<FarmerResponseDto> updateFarmer(
            @PathVariable Long id,
            @Valid @RequestBody FarmerRequestDto requestDto) {
        FarmerResponseDto updatedFarmer = farmerService.updateFarmer(id, requestDto);
        return ResponseEntity.ok(updatedFarmer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a farmer by ID")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.noContent().build();
    }
}
