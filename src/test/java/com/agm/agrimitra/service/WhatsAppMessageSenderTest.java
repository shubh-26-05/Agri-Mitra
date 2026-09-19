package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.WhatsAppSendResult;
import com.agm.agrimitra.service.impl.WhatsAppMessageSenderImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WhatsAppMessageSenderTest {

    private WhatsAppMessageSenderImpl messageSender;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ACCESS_TOKEN = "EAAGm0PX4ZCpsBOtestToken";
    private static final String PHONE_NUMBER_ID = "109876543212345";
    private static final String API_BASE_URL = "https://graph.facebook.com/v25.0";

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        this.mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        this.messageSender = new WhatsAppMessageSenderImpl(objectMapper, restClient, ACCESS_TOKEN, PHONE_NUMBER_ID, API_BASE_URL);
    }

    @Test
    @DisplayName("Should successfully send message and return message ID when Meta returns 200 OK")
    void testSendTextMessage_Success() {
        String recipient = "919876543210";
        String message = "Hello from AgriMitra!";
        String mockMetaResponse = """
                {
                  "messaging_product": "whatsapp",
                  "contacts": [
                    {
                      "input": "919876543210",
                      "wa_id": "919876543210"
                    }
                  ],
                  "messages": [
                    {
                      "id": "wamid.HBgLMTE1ODI="
                    }
                  ]
                }
                """;

        mockServer.expect(requestTo("https://graph.facebook.com/v25.0/109876543212345/messages"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + ACCESS_TOKEN))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, containsString(MediaType.APPLICATION_JSON_VALUE)))
                .andExpect(content().string(containsString("\"messaging_product\":\"whatsapp\"")))
                .andExpect(content().string(containsString("\"to\":\"919876543210\"")))
                .andExpect(content().string(containsString("\"text\":{\"body\":\"Hello from AgriMitra!\"}")))
                .andRespond(withSuccess(mockMetaResponse, MediaType.APPLICATION_JSON));

        WhatsAppSendResult result = messageSender.sendTextMessage(recipient, message);

        assertTrue(result.isSuccess());
        assertEquals("wamid.HBgLMTE1ODI=", result.getMessageId());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should handle Meta API 400 Bad Request error gracefully without throwing")
    void testSendTextMessage_MetaApiError_ReturnsFailure() {
        String recipient = "919876543210";
        String message = "Hello!";
        String errorJson = """
                {
                  "error": {
                    "message": "(#131030) Recipient phone number not in allowed list",
                    "type": "OAuthException",
                    "code": 131030
                  }
                }
                """;

        mockServer.expect(requestTo("https://graph.facebook.com/v25.0/109876543212345/messages"))
                .andRespond(withBadRequest().body(errorJson).contentType(MediaType.APPLICATION_JSON));

        WhatsAppSendResult result = messageSender.sendTextMessage(recipient, message);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("131030") || result.getErrorMessage().contains("400"));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should fail validation when access token is missing")
    void testSendTextMessage_MissingToken_ReturnsFailure() {
        WhatsAppMessageSenderImpl unconfiguredSender = new WhatsAppMessageSenderImpl(
                objectMapper, RestClient.builder().build(), null, PHONE_NUMBER_ID, API_BASE_URL);

        WhatsAppSendResult result = unconfiguredSender.sendTextMessage("919876543210", "Hello");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("access token"));
    }

    @Test
    @DisplayName("Should fail validation when phone number ID is missing")
    void testSendTextMessage_MissingPhoneNumberId_ReturnsFailure() {
        WhatsAppMessageSenderImpl unconfiguredSender = new WhatsAppMessageSenderImpl(
                objectMapper, RestClient.builder().build(), ACCESS_TOKEN, "", API_BASE_URL);

        WhatsAppSendResult result = unconfiguredSender.sendTextMessage("919876543210", "Hello");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("phone number ID"));
    }

    @Test
    @DisplayName("Should fail validation when recipient is blank")
    void testSendTextMessage_BlankRecipient_ReturnsFailure() {
        WhatsAppSendResult result = messageSender.sendTextMessage("   ", "Hello");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Recipient WhatsApp ID is required"));
    }

    @Test
    @DisplayName("Should fail validation when message text is blank")
    void testSendTextMessage_BlankMessage_ReturnsFailure() {
        WhatsAppSendResult result = messageSender.sendTextMessage("919876543210", "  ");

        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("Message text cannot be blank"));
    }
}
