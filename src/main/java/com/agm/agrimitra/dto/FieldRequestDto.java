package com.agm.agrimitra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldRequestDto {

    @NotBlank(message = "Field name is required and cannot be blank")
    private String fieldName;

    @NotNull(message = "Area in acres is required")
    @Positive(message = "Area in acres must be greater than zero")
    private Double areaInAcres;
}
