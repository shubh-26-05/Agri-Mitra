package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.WeatherDataDto;

import java.util.Optional;

public interface WeatherService {

    /**
     * Fetches current weather data for a given district and state in India.
     *
     * @param district the district name
     * @param state    the state name
     * @return an Optional containing WeatherDataDto if successful, or Optional.empty() on failure
     */
    Optional<WeatherDataDto> getCurrentWeather(String district, String state);
}
