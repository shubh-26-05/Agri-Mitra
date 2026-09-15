package com.agm.agrimitra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerRequestDto {

    @NotBlank(message = "Farmer name is required and cannot be blank")
    private String name;

    @NotBlank(message = "Phone number is required and cannot be blank")
    private String phoneNumber;

    @Email(message = "Email must be a valid email address")
    private String email;

    @Valid
    private AddressDto location;
}
