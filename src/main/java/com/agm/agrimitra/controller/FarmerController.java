package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.FarmerRequestDto;
import com.agm.agrimitra.dto.FarmerResponseDto;
import com.agm.agrimitra.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farmers")
@RequiredArgsConstructor
@Tag(name = "Farmer Controller", description = "Endpoints for managing farmers")
@SecurityRequirement(name = "bearerAuth")
public class FarmerController {

    private final FarmerService farmerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new farmer profile (Admin only; farmers register via /api/auth/register-farmer)")
    public ResponseEntity<FarmerResponseDto> createFarmer(@Valid @RequestBody FarmerRequestDto requestDto) {
        FarmerResponseDto createdFarmer = farmerService.createFarmer(requestDto);
        return new ResponseEntity<>(createdFarmer, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all farmers with pagination and sorting (Admin only)")
    public ResponseEntity<Page<FarmerResponseDto>> getAllFarmers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        int cappedSize = Math.min(Math.max(size, 1), 100);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), cappedSize, Sort.by(direction, sortBy));

        Page<FarmerResponseDto> farmers = farmerService.getAllFarmers(pageable);
        return ResponseEntity.ok(farmers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFarmer(#id)")
    @Operation(summary = "Get farmer by ID (Admin or own profile)")
    public ResponseEntity<FarmerResponseDto> getFarmerById(@PathVariable Long id) {
        FarmerResponseDto farmer = farmerService.getFarmerById(id);
        return ResponseEntity.ok(farmer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFarmer(#id)")
    @Operation(summary = "Update an existing farmer (Admin or own profile)")
    public ResponseEntity<FarmerResponseDto> updateFarmer(
            @PathVariable Long id,
            @Valid @RequestBody FarmerRequestDto requestDto) {
        FarmerResponseDto updatedFarmer = farmerService.updateFarmer(id, requestDto);
        return ResponseEntity.ok(updatedFarmer);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a farmer by ID (Admin only)")
    public ResponseEntity<Void> deleteFarmer(@PathVariable Long id) {
        farmerService.deleteFarmer(id);
        return ResponseEntity.noContent().build();
    }
}
