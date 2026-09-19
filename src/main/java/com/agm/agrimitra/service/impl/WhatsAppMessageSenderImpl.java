package com.agm.agrimitra.service.impl;

import com.agm.agrimitra.dto.WhatsAppSendMessageDto;
import com.agm.agrimitra.dto.WhatsAppSendResult;
import com.agm.agrimitra.service.WhatsAppMessageSender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
public class WhatsAppMessageSenderImpl implements WhatsAppMessageSender {

    @Value("${whatsapp.access-token:}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.api-base-url:https://graph.facebook.com/v25.0}")
    private String apiBaseUrl;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public WhatsAppMessageSenderImpl(ObjectMapper objectMapper) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    // Secondary constructor for unit testing or custom RestClient injection
    public WhatsAppMessageSenderImpl(ObjectMapper objectMapper, RestClient restClient,
                                  String accessToken, String phoneNumberId, String apiBaseUrl) {
        this.objectMapper = (objectMapper != null) ? objectMapper : new ObjectMapper();
        this.restClient = restClient;
        this.accessToken = accessToken;
        this.phoneNumberId = phoneNumberId;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public WhatsAppSendResult sendTextMessage(String recipientWaId, String messageText) {
        if (accessToken == null || accessToken.isBlank()) {
            log.warn("WhatsApp access token is not configured. Skipping message send to {}.", recipientWaId);
            return WhatsAppSendResult.failure("WhatsApp access token is not configured (whatsapp.access-token).");
        }

        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            log.warn("WhatsApp phone number ID is not configured. Skipping message send to {}.", recipientWaId);
            return WhatsAppSendResult.failure("WhatsApp phone number ID is not configured (whatsapp.phone-number-id).");
        }

        if (recipientWaId == null || recipientWaId.isBlank()) {
            log.warn("Recipient WhatsApp ID is blank. Skipping message send.");
            return WhatsAppSendResult.failure("Recipient WhatsApp ID is required.");
        }

        if (messageText == null || messageText.isBlank()) {
            log.warn("Message text is blank. Skipping message send to {}.", recipientWaId);
            return WhatsAppSendResult.failure("Message text cannot be blank.");
        }

        String cleanBaseUrl = apiBaseUrl != null && apiBaseUrl.endsWith("/")
                ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1)
                : (apiBaseUrl != null ? apiBaseUrl : "https://graph.facebook.com/v25.0");

        String endpointUrl = String.format("%s/%s/messages", cleanBaseUrl.trim(), phoneNumberId.trim());
        WhatsAppSendMessageDto requestPayload = WhatsAppSendMessageDto.create(recipientWaId.trim(), messageText);

        try {
            String requestJson = objectMapper.writeValueAsString(requestPayload);

            String responseBody = restClient.post()
                    .uri(URI.create(endpointUrl))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(String.class);

            String messageId = null;
            if (responseBody != null && !responseBody.isBlank()) {
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode messagesNode = root.path("messages");
                if (messagesNode.isArray() && !messagesNode.isEmpty()) {
                    messageId = messagesNode.get(0).path("id").asText(null);
                }
            }

            log.info("Successfully sent WhatsApp message to {}. Meta Message ID: {}", recipientWaId, messageId);
            return WhatsAppSendResult.success(messageId);

        } catch (RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("Meta Graph API error while sending WhatsApp message to {}. HTTP Status: {}, Response: {}",
                    recipientWaId, e.getStatusCode(), errorBody);
            return WhatsAppSendResult.failure(String.format("Meta API error: HTTP %s - %s", e.getStatusCode(), errorBody));

        } catch (ResourceAccessException e) {
            log.error("Network or timeout error while contacting Meta Graph API for recipient {}: {}",
                    recipientWaId, e.getMessage());
            return WhatsAppSendResult.failure("Network or timeout error: " + e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected error while sending WhatsApp message to {}: {}",
                    recipientWaId, e.getMessage(), e);
            return WhatsAppSendResult.failure("Unexpected error: " + e.getMessage());
        }
    }
}
