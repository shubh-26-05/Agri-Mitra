package com.agm.agrimitra.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WhatsAppWebhookControllerTest {

    private MockMvc mockMvc;
    private WhatsAppWebhookController controller;

    private static final String VALID_VERIFY_TOKEN = "my-secret-whatsapp-verify-token-12345";

    @BeforeEach
    void setUp() {
        controller = new WhatsAppWebhookController();
        ReflectionTestUtils.setField(controller, "configuredVerifyToken", VALID_VERIFY_TOKEN);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testVerifyWebhook_Success_ReturnsChallengePlainText() throws Exception {
        String challenge = "1158201444";

        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", VALID_VERIFY_TOKEN)
                        .param("hub.challenge", challenge))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(challenge));
    }

    @Test
    void testVerifyWebhook_InvalidToken_Returns403() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "subscribe")
                        .param("hub.verify_token", "wrong-token")
                        .param("hub.challenge", "1158201444"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testVerifyWebhook_InvalidMode_Returns403() throws Exception {
        mockMvc.perform(get("/api/whatsapp/webhook")
                        .param("hub.mode", "unsubscribe")
                        .param("hub.verify_token", VALID_VERIFY_TOKEN)
                        .param("hub.challenge", "1158201444"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testReceiveWebhook_Returns200AndEmptyBody() throws Exception {
        String sampleMetaPayload = """
                {
                  "object": "whatsapp_business_account",
                  "entry": [
                    {
                      "id": "123456789",
                      "changes": [
                        {
                          "value": {
                            "messaging_product": "whatsapp"
                          },
                          "field": "messages"
                        }
                      ]
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleMetaPayload))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}
