package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.WhatsAppSendResult;
import com.agm.agrimitra.service.WhatsAppMessageSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TempWhatsAppTestControllerTest {

    private MockMvc mockMvc;
    private WhatsAppMessageSender whatsAppMessageSender;

    @BeforeEach
    void setUp() {
        whatsAppMessageSender = mock(WhatsAppMessageSender.class);
        TempWhatsAppTestController controller = new TempWhatsAppTestController(whatsAppMessageSender);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testSendTestMessage_Success() throws Exception {
        when(whatsAppMessageSender.sendTextMessage(eq("919876543210"), eq("Test Message")))
                .thenReturn(WhatsAppSendResult.success("wamid.12345"));

        mockMvc.perform(post("/api/test/whatsapp/send")
                        .param("to", "919876543210")
                        .param("message", "Test Message")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.messageId").value("wamid.12345"));

        verify(whatsAppMessageSender).sendTextMessage("919876543210", "Test Message");
    }
}
