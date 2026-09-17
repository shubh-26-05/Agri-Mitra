package com.agm.agrimitra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDataDto {

    private Double temperature;
    private Integer humidity;
    private String weatherCondition;
    private Double windSpeed;
    private LocalDateTime fetchedAt;
}
