package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.service.impl.WeatherServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

class WeatherServiceTest {

    private WeatherServiceImpl weatherService;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String MOCK_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        this.weatherService = new WeatherServiceImpl(objectMapper, restClient, MOCK_API_KEY, BASE_URL);
    }

    @Test
    @DisplayName("Should successfully parse weather response when API returns 200 OK")
    void testGetCurrentWeather_Success() {
        String mockJsonResponse = """
                {
                    "weather": [
                        {
                            "id": 800,
                            "main": "Clear",
                            "description": "clear sky",
                            "icon": "01d"
                        }
                    ],
                    "main": {
                        "temp": 28.5,
                        "humidity": 62,
                        "pressure": 1012
                    },
                    "wind": {
                        "speed": 4.12
                    }
                }
                """;

        mockServer.expect(requestTo(containsString("q=Pune,Maharashtra,IN")))
                .andRespond(withSuccess(mockJsonResponse, MediaType.APPLICATION_JSON));

        Optional<WeatherDataDto> result = weatherService.getCurrentWeather("Pune", "Maharashtra");

        assertTrue(result.isPresent());
        WeatherDataDto data = result.get();
        assertEquals(28.5, data.getTemperature());
        assertEquals(62, data.getHumidity());
        assertEquals("clear sky", data.getWeatherCondition());
        assertEquals(4.12, data.getWindSpeed());
        assertNotNull(data.getFetchedAt());

        mockServer.verify();
    }

    @Test
    @DisplayName("Should return Optional.empty() when OpenWeatherMap returns 404 city not found")
    void testGetCurrentWeather_NotFound() {
        mockServer.expect(requestTo(containsString("q=InvalidCity,UnknownState,IN")))
                .andRespond(withResourceNotFound());

        Optional<WeatherDataDto> result = weatherService.getCurrentWeather("InvalidCity", "UnknownState");

        assertFalse(result.isPresent());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should return Optional.empty() when API key is blank")
    void testGetCurrentWeather_BlankApiKey() {
        WeatherServiceImpl unconfiguredService = new WeatherServiceImpl(objectMapper, RestClient.create(), "", BASE_URL);

        Optional<WeatherDataDto> result = unconfiguredService.getCurrentWeather("Pune", "Maharashtra");

        assertFalse(result.isPresent());
    }

    @Test
    void testLiveOpenWeatherCall() {
        String apiKey = System.getenv("OPENWEATHER_API_KEY"); // or set directly in .env
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        WeatherService liveService = new WeatherServiceImpl(
                new ObjectMapper(),
                RestClient.create(),
                apiKey,
                "https://api.openweathermap.org/data/2.5/weather"
        );

        Optional<WeatherDataDto> weather = liveService.getCurrentWeather("Pune", "Maharashtra");
        weather.ifPresent(w -> System.out.println("Live Weather: " + w));
        assertTrue(weather.isPresent());
    }
}
