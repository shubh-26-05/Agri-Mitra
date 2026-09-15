package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.SoilExtractionResult;
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
            log.warn("Gemini API key is not configured. Falling back to extractionSuccessful=false for regional estimation.");
            return SoilExtractionResult.builder().extractionSuccessful(false).build();
        }

        try {
            String locationStr = buildLocationString(village, district, state);

            String prompt = "You are an expert agricultural soil scientist with access to Google Search. " +
                    "Search for regional soil test benchmarks, ICAR soil surveys, State Department of Agriculture reports, or Soil Health Card district averages for the following location in India: " + locationStr + ".\n\n" +
                    "Estimate typical or average agricultural soil test values for the 12 standard parameters of India's Soil Health Card:\n" +
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
                    "Instructions:\n" +
                    "- Return numerical values only for each parameter (numbers only, without units).\n" +
                    "- If a parameter cannot be reliably estimated from available regional data, leave that specific field as null.\n" +
                    "- Set extractionSuccessful to true if you could estimate at least some parameters for this region, or false if no reliable regional soil data could be found.";

            Map<String, Object> textPart = Map.of("text", prompt);
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
