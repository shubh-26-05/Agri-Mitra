package com.agm.agrimitra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerResponseDto {

    private Long id;
    private String name;
    private String phoneNumber;
    private String email;
    private AddressDto location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
