package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.service.WeatherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.base-url:https://api.openweathermap.org/data/2.5/weather}")
    private String baseUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public WeatherServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // Secondary constructor for testing or custom RestClient
    public WeatherServiceImpl(ObjectMapper objectMapper, RestClient restClient, String apiKey, String baseUrl) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<WeatherDataDto> getCurrentWeather(String district, String state) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenWeatherMap API key is not configured. Skipping weather fetch for district='{}', state='{}'.", district, state);
            return Optional.empty();
        }

        if (district == null || district.isBlank()) {
            log.warn("District name is required but was blank. Skipping weather fetch.");
            return Optional.empty();
        }

        String locationQuery = buildLocationQuery(district, state);

        try {
            URI uri = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("q", locationQuery)
                    .queryParam("appid", apiKey.trim())
                    .queryParam("units", "metric")
                    .build()
                    .toUri();

            String responseBody = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("Empty response received from OpenWeatherMap for district='{}', state='{}'.", district, state);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseBody);

            Double temperature = root.path("main").hasNonNull("temp") ? root.path("main").path("temp").asDouble() : null;
            Integer humidity = root.path("main").hasNonNull("humidity") ? root.path("main").path("humidity").asInt() : null;
            Double windSpeed = root.path("wind").hasNonNull("speed") ? root.path("wind").path("speed").asDouble() : null;

            String weatherCondition = null;
            JsonNode weatherArray = root.path("weather");
            if (weatherArray.isArray() && !weatherArray.isEmpty()) {
                JsonNode firstWeather = weatherArray.get(0);
                if (firstWeather.hasNonNull("description")) {
                    weatherCondition = firstWeather.path("description").asText();
                } else if (firstWeather.hasNonNull("main")) {
                    weatherCondition = firstWeather.path("main").asText();
                }
            }

            WeatherDataDto weatherData = WeatherDataDto.builder()
                    .temperature(temperature)
                    .humidity(humidity)
                    .weatherCondition(weatherCondition)
                    .windSpeed(windSpeed)
                    .fetchedAt(LocalDateTime.now())
                    .build();

            return Optional.of(weatherData);

        } catch (RestClientResponseException e) {
            log.warn("OpenWeatherMap HTTP error for district='{}', state='{}': status={}", district, state, e.getStatusCode());
            return Optional.empty();
        } catch (ResourceAccessException e) {
            log.warn("OpenWeatherMap timeout/connection failure for district='{}', state='{}': {}", district, state, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to retrieve weather for district='{}', state='{}': {}", district, state, e.getMessage());
            return Optional.empty();
        }
    }

    private String buildLocationQuery(String district, String state) {
        StringBuilder sb = new StringBuilder();
        sb.append(district.trim());
        if (state != null && !state.isBlank()) {
            sb.append(",").append(state.trim());
        }
        sb.append(",IN");
        return sb.toString();
    }
}
