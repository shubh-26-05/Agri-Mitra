package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.FieldRequestDto;
import com.agm.agrimitra.dto.FieldResponseDto;
import com.agm.agrimitra.service.FieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Field Controller", description = "Endpoints for managing fields")
@SecurityRequirement(name = "bearerAuth")
public class FieldController {

    private final FieldService fieldService;

    @PostMapping("/api/farmers/{farmerId}/fields")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFarmer(#farmerId)")
    @Operation(summary = "Create a field for a specific farmer (Admin or farmer owner)")
    public ResponseEntity<FieldResponseDto> createField(
            @PathVariable Long farmerId,
            @Valid @RequestBody FieldRequestDto requestDto) {
        FieldResponseDto createdField = fieldService.createField(farmerId, requestDto);
        return new ResponseEntity<>(createdField, HttpStatus.CREATED);
    }

    @GetMapping("/api/farmers/{farmerId}/fields")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFarmer(#farmerId)")
    @Operation(summary = "Get all fields belonging to a specific farmer (Admin or farmer owner)")
    public ResponseEntity<List<FieldResponseDto>> getFieldsByFarmerId(@PathVariable Long farmerId) {
        List<FieldResponseDto> fields = fieldService.getFieldsByFarmerId(farmerId);
        return ResponseEntity.ok(fields);
    }

    @GetMapping("/api/fields/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#id)")
    @Operation(summary = "Get a field by ID (Admin or field owner)")
    public ResponseEntity<FieldResponseDto> getFieldById(@PathVariable Long id) {
        FieldResponseDto field = fieldService.getFieldById(id);
        return ResponseEntity.ok(field);
    }

    @PutMapping("/api/fields/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#id)")
    @Operation(summary = "Update an existing field (Admin or field owner)")
    public ResponseEntity<FieldResponseDto> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldRequestDto requestDto) {
        FieldResponseDto updatedField = fieldService.updateField(id, requestDto);
        return ResponseEntity.ok(updatedField);
    }

    @DeleteMapping("/api/fields/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isFieldOwner(#id)")
    @Operation(summary = "Delete a field by ID (Admin or field owner)")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ResponseEntity.noContent().build();
    }
}
