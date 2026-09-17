package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.CropRecommendationResult;
import com.agm.agrimitra.dto.SoilExtractionResult;
import com.agm.agrimitra.dto.WeatherDataDto;
import com.agm.agrimitra.entity.FertilizerUsage;
import com.agm.agrimitra.entity.SoilData;
import com.agm.agrimitra.service.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    private static final String GEMINI_API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash}")
    private String modelName;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public GeminiServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public SoilExtractionResult extractSoilDataFromImage(byte[] imageBytes, String mimeType) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Falling back to extractionSuccessful=false.");
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }

        if (imageBytes == null || imageBytes.length == 0) {
            log.warn("Empty or null image bytes provided. Extraction aborted.");
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }

        try {
            String normalizedMimeType = normalizeMimeType(mimeType);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> textPart = Map.of(
                    "text", "You are an expert agronomist and OCR specialist. Analyze this uploaded image of an Indian Soil Health Card (SHC). " +
                            "Extract the quantitative test values for the 12 standard parameters: \n" +
                            "1. Nitrogen (N) (kg/ha)\n" +
                            "2. Phosphorus (P) (kg/ha)\n" +
                            "3. Potassium (K) (kg/ha)\n" +
                            "4. pH\n" +
                            "5. Electrical Conductivity (EC) (dS/m)\n" +
                            "6. Organic Carbon (OC) (%)\n" +
                            "7. Sulphur (S) (ppm or kg/ha)\n" +
                            "8. Zinc (Zn) (ppm)\n" +
                            "9. Iron (Fe) (ppm)\n" +
                            "10. Copper (Cu) (ppm)\n" +
                            "11. Manganese (Mn) (ppm)\n" +
                            "12. Boron (B) (ppm)\n\n" +
                            "If the image is not a valid Soil Health Card or no numerical soil test readings can be discerned, set extractionSuccessful to false and all parameter fields to null. " +
                            "If it is a valid card, extract the numerical readings (numbers only, without units) and set extractionSuccessful to true."
            );

            Map<String, Object> inlineData = Map.of(
                    "mimeType", normalizedMimeType,
                    "data", base64Image
            );
            Map<String, Object> imagePart = Map.of("inlineData", inlineData);

            Map<String, Object> content = Map.of("parts", List.of(textPart, imagePart));

            Map<String, Object> schemaProperties = new LinkedHashMap<>();
            for (String field : List.of("nitrogenLevel", "phosphorusLevel", "potassiumLevel", "phLevel",
                    "electricalConductivity", "organicCarbon", "sulphurLevel", "zincLevel",
                    "ironLevel", "copperLevel", "manganeseLevel", "boronLevel")) {
                schemaProperties.put(field, Map.of("type", "NUMBER", "nullable", true));
            }
            schemaProperties.put("extractionSuccessful", Map.of("type", "BOOLEAN"));

            Map<String, Object> responseSchema = Map.of(
                    "type", "OBJECT",
                    "properties", schemaProperties,
                    "required", List.of("extractionSuccessful")
            );

            Map<String, Object> generationConfig = Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", responseSchema,
                    "temperature", 0.1
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            String activeModel = (modelName != null && !modelName.isBlank()) ? modelName.trim() : "gemini-2.0-flash";
            URI requestUri = URI.create(String.format(GEMINI_API_URL_TEMPLATE, activeModel));

            String responseJson = restClient.post()
                    .uri(requestUri)
                    .header("x-goog-api-key", apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode textNode = rootNode.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                log.warn("Gemini returned empty candidate text.");
                return SoilExtractionResult.builder().extractionSuccessful(false).build();
            }

            String extractionText = cleanJsonText(textNode.asText());
            return objectMapper.readValue(extractionText, SoilExtractionResult.class);

        } catch (RestClientResponseException e) {
            log.error("Gemini API HTTP error: status={}, responseBody={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        } catch (Exception e) {
            log.error("Failed to extract soil data via Gemini OCR: {}", e.getMessage(), e);
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }
    }

    @Override
    public SoilExtractionResult estimateSoilDataFromRegion(String village, String district, String state) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Regional estimation aborted.");
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }

        try {
            String location = buildLocationString(village, district, state);
            String promptText = String.format(
                    "Search for typical or average agricultural soil test values from official Indian government Soil Health Card data, " +
                            "ICAR reports, state agriculture portals, or Krishi Vigyan Kendra (KVK) records for the region: %s. " +
                            "Extract the average/typical numerical values for the 12 standard parameters: " +
                            "1. Nitrogen (N) (kg/ha), 2. Phosphorus (P) (kg/ha), 3. Potassium (K) (kg/ha), " +
                            "4. pH, 5. Electrical Conductivity (EC) (dS/m), 6. Organic Carbon (OC) (%%), " +
                            "7. Sulphur (S) (ppm or kg/ha), 8. Zinc (Zn) (ppm), 9. Iron (Fe) (ppm), " +
                            "10. Copper (Cu) (ppm), 11. Manganese (Mn) (ppm), 12. Boron (B) (ppm). " +
                            "Return estimated typical values based on regional soil classification and geography. " +
                            "If reasonable estimates can be determined, set extractionSuccessful to true and fill in the parameters. " +
                            "Only set extractionSuccessful to false if the region is completely unrecognized.",
                    location
            );

            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> content = Map.of("parts", List.of(textPart));

            Map<String, Object> schemaProperties = new LinkedHashMap<>();
            for (String field : List.of("nitrogenLevel", "phosphorusLevel", "potassiumLevel", "phLevel",
                    "electricalConductivity", "organicCarbon", "sulphurLevel", "zincLevel",
                    "ironLevel", "copperLevel", "manganeseLevel", "boronLevel")) {
                schemaProperties.put(field, Map.of("type", "NUMBER", "nullable", true));
            }
            schemaProperties.put("extractionSuccessful", Map.of("type", "BOOLEAN"));

            Map<String, Object> responseSchema = Map.of(
                    "type", "OBJECT",
                    "properties", schemaProperties,
                    "required", List.of("extractionSuccessful")
            );

            Map<String, Object> generationConfig = Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", responseSchema,
                    "temperature", 0.2
            );

            List<Map<String, Object>> tools = List.of(
                    Map.of("google_search", Map.of())
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "tools", tools,
                    "generationConfig", generationConfig
            );

            String activeModel = (modelName != null && !modelName.isBlank()) ? modelName.trim() : "gemini-2.0-flash";
            URI requestUri = URI.create(String.format(GEMINI_API_URL_TEMPLATE, activeModel));

            String responseJson = restClient.post()
                    .uri(requestUri)
                    .header("x-goog-api-key", apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode partsNode = rootNode.path("candidates").path(0).path("content").path("parts");
            String rawText = null;
            if (partsNode.isArray()) {
                for (JsonNode part : partsNode) {
                    if (part.has("text") && !part.path("text").asText().isBlank()) {
                        rawText = part.path("text").asText();
                        break;
                    }
                }
            }

            if (rawText == null || rawText.isBlank()) {
                log.warn("Gemini returned empty candidate text for regional soil estimation.");
                return SoilExtractionResult.builder().extractionSuccessful(false).build();
            }

            String extractionText = cleanJsonText(rawText);
            return objectMapper.readValue(extractionText, SoilExtractionResult.class);

        } catch (RestClientResponseException e) {
            log.error("Gemini regional estimation HTTP error: status={}, responseBody={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        } catch (Exception e) {
            log.error("Failed to estimate regional soil data via Gemini search grounding: {}", e.getMessage(), e);
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }
    }

    @Override
    public CropRecommendationResult generateCropRecommendation(
            SoilData soilData,
            WeatherDataDto weather,
            List<FertilizerUsage> recentFertilizerHistory,
            Double fieldAreaInAcres,
            String stalenessTier) {

        if (apiKey == null || apiKey.isBlank()) {
            log.error("Gemini API key is not configured.");
            throw new IllegalStateException("Gemini API key is not configured. Cannot generate crop recommendation.");
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an expert agronomist providing precise crop recommendations and fertilizer advice for Indian farmers.\n\n");

            prompt.append("--- FIELD & LAND PROFILE ---\n");
            prompt.append("Field Area: ").append(fieldAreaInAcres != null ? fieldAreaInAcres + " acres" : "Unknown").append("\n\n");

            prompt.append("--- SOIL HEALTH CARD (12 PARAMETERS) ---\n");
            if (soilData != null) {
                prompt.append("1. Nitrogen (N): ").append(formatNutrient(soilData.getNitrogenLevel(), "kg/ha")).append("\n");
                prompt.append("2. Phosphorus (P): ").append(formatNutrient(soilData.getPhosphorusLevel(), "kg/ha")).append("\n");
                prompt.append("3. Potassium (K): ").append(formatNutrient(soilData.getPotassiumLevel(), "kg/ha")).append("\n");
                prompt.append("4. pH Level: ").append(soilData.getPhLevel() != null ? soilData.getPhLevel() : "Unknown").append("\n");
                prompt.append("5. Electrical Conductivity (EC): ").append(formatNutrient(soilData.getElectricalConductivity(), "dS/m")).append("\n");
                prompt.append("6. Organic Carbon (OC): ").append(formatNutrient(soilData.getOrganicCarbon(), "%")).append("\n");
                prompt.append("7. Sulphur (S): ").append(formatNutrient(soilData.getSulphurLevel(), "ppm")).append("\n");
                prompt.append("8. Zinc (Zn): ").append(formatNutrient(soilData.getZincLevel(), "ppm")).append("\n");
                prompt.append("9. Iron (Fe): ").append(formatNutrient(soilData.getIronLevel(), "ppm")).append("\n");
                prompt.append("10. Copper (Cu): ").append(formatNutrient(soilData.getCopperLevel(), "ppm")).append("\n");
                prompt.append("11. Manganese (Mn): ").append(formatNutrient(soilData.getManganeseLevel(), "ppm")).append("\n");
                prompt.append("12. Boron (B): ").append(formatNutrient(soilData.getBoronLevel(), "ppm")).append("\n");
            } else {
                prompt.append("No soil test parameters available.\n");
            }

            prompt.append("\n--- SOIL DATA FRESHNESS & STALENESS TIER ---\n");
            prompt.append("Staleness Tier: ").append(stalenessTier != null ? stalenessTier : "UNKNOWN").append("\n");
            if ("HIGH_STALE".equalsIgnoreCase(stalenessTier)) {
                prompt.append("IMPORTANT: The soil test data provided above is between 24 and 36 months old and may be significantly outdated. "
                        + "You MUST factor this substantial staleness into a notably reduced confidence score (e.g. notably reduced below normal, typically <= 0.60) "
                        + "and explicitly mention this caveat and staleness in your reasoning, advising the farmer that the recommendation relies on deprecated soil tests.\n");
            }

            prompt.append("\n--- CURRENT WEATHER CONDITIONS ---\n");
            if (weather != null) {
                prompt.append("Temperature: ").append(weather.getTemperature() != null ? weather.getTemperature() + " °C" : "Unknown").append("\n");
                prompt.append("Humidity: ").append(weather.getHumidity() != null ? weather.getHumidity() + " %" : "Unknown").append("\n");
                prompt.append("Weather Condition: ").append(weather.getWeatherCondition() != null ? weather.getWeatherCondition() : "Unknown").append("\n");
                prompt.append("Wind Speed: ").append(weather.getWindSpeed() != null ? weather.getWindSpeed() + " m/s" : "Unknown").append("\n");
            } else {
                prompt.append("No real-time weather context is available. Proceed with recommendation based on soil characteristics, field profile, and general agronomic principles without weather context.\n");
            }

            prompt.append("\n--- RECENT FERTILIZER USAGE HISTORY ---\n");
            if (recentFertilizerHistory != null && !recentFertilizerHistory.isEmpty()) {
                for (FertilizerUsage usage : recentFertilizerHistory) {
                    prompt.append(String.format("- Fertilizer: %s, Quantity: %s %s, Applied Date: %s\n",
                            usage.getFertilizerType(),
                            usage.getQuantityUsed(),
                            usage.getQuantityUnit(),
                            usage.getApplicationDate() != null ? usage.getApplicationDate() : "Unknown"));
                }
            } else {
                prompt.append("No prior fertilizer application on record. Treat this as no prior fertilizer application on record.\n");
            }

            prompt.append("\n--- INSTRUCTIONS ---\n");
            prompt.append("Based on the comprehensive profile above, generate a recommendation JSON with:\n");
            prompt.append("1. recommendedCrop (String): The single most suitable crop for this land.\n");
            prompt.append("2. confidenceScore (Double between 0.0 and 1.0): Your confidence in the recommendation.\n");
            prompt.append("3. reasoning (String): Clear agronomic justification explaining the recommendation.\n");
            prompt.append("4. fertilizerAdvice (String): Clear, actionable next fertilizer application steps and dosage given soil nutrients and history.\n");

            Map<String, Object> textPart = Map.of("text", prompt.toString());
            Map<String, Object> content = Map.of("parts", List.of(textPart));

            Map<String, Object> schemaProperties = new LinkedHashMap<>();
            schemaProperties.put("recommendedCrop", Map.of("type", "STRING"));
            schemaProperties.put("confidenceScore", Map.of("type", "NUMBER"));
            schemaProperties.put("reasoning", Map.of("type", "STRING"));
            schemaProperties.put("fertilizerAdvice", Map.of("type", "STRING"));

            Map<String, Object> responseSchema = Map.of(
                    "type", "OBJECT",
                    "properties", schemaProperties,
                    "required", List.of("recommendedCrop", "confidenceScore", "reasoning", "fertilizerAdvice")
            );

            Map<String, Object> generationConfig = Map.of(
                    "responseMimeType", "application/json",
                    "responseSchema", responseSchema,
                    "temperature", 0.2
            );

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(content),
                    "generationConfig", generationConfig
            );

            String activeModel = (modelName != null && !modelName.isBlank()) ? modelName.trim() : "gemini-2.0-flash";
            URI requestUri = URI.create(String.format(GEMINI_API_URL_TEMPLATE, activeModel));

            String responseJson = restClient.post()
                    .uri(requestUri)
                    .header("x-goog-api-key", apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseJson);
            JsonNode partsNode = rootNode.path("candidates").path(0).path("content").path("parts");
            String rawText = null;
            if (partsNode.isArray()) {
                for (JsonNode part : partsNode) {
                    if (part.has("text") && !part.path("text").asText().isBlank()) {
                        rawText = part.path("text").asText();
                        break;
                    }
                }
            }

            if (rawText == null || rawText.isBlank()) {
                log.warn("Gemini returned empty candidate text for crop recommendation.");
                throw new RuntimeException("Gemini returned empty candidate text for crop recommendation.");
            }

            String cleanedJson = cleanJsonText(rawText);
            return objectMapper.readValue(cleanedJson, CropRecommendationResult.class);

        } catch (RestClientResponseException e) {
            log.error("Gemini crop recommendation HTTP error: status={}, responseBody={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Gemini API error during recommendation generation: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Failed to generate crop recommendation via Gemini: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate crop recommendation: " + e.getMessage(), e);
        }
    }

    private String formatNutrient(Double value, String unit) {
        return value != null ? value + " " + unit : "Unknown";
    }

    private String buildLocationString(String village, String district, String state) {
        StringBuilder sb = new StringBuilder();
        if (village != null && !village.isBlank()) {
            sb.append("Village: ").append(village.trim()).append(", ");
        }
        if (district != null && !district.isBlank()) {
            sb.append("District: ").append(district.trim()).append(", ");
        }
        if (state != null && !state.isBlank()) {
            sb.append("State: ").append(state.trim()).append(", ");
        }
        sb.append("India");
        return sb.toString();
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return "image/jpeg";
        }
        String cleaned = mimeType.split(";")[0].trim().toLowerCase();
        if ("image/jpg".equals(cleaned)) {
            return "image/jpeg";
        }
        return cleaned;
    }

    private String cleanJsonText(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }
}
