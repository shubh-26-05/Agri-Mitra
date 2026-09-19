package com.agm.agrimitra.controller;

import com.agm.agrimitra.dto.WhatsAppSendResult;
import com.agm.agrimitra.service.WhatsAppMessageSender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Temporary test controller to manually test WhatsApp message sending via Meta Graph API.
 * This can be safely removed or disabled in production.
 */
@RestController
@RequestMapping("/api/test/whatsapp")
@RequiredArgsConstructor
@Tag(name = "Temp WhatsApp Test Controller", description = "Temporary endpoint to manually test sending WhatsApp messages")
public class TempWhatsAppTestController {

    private final WhatsAppMessageSender whatsAppMessageSender;

    /**
     * Sends a test WhatsApp message.
     *
     * @param to      Recipient WhatsApp ID (e.g., 919876543210)
     * @param message Message content to send
     * @return WhatsAppSendResult containing success status, message ID, or error message
     */
    @PostMapping("/send")
    @Operation(summary = "Temporary manual test endpoint to send a WhatsApp message")
    public ResponseEntity<WhatsAppSendResult> sendTestMessage(
            @RequestParam("to") String to,
            @RequestParam("message") String message) {

        WhatsAppSendResult result = whatsAppMessageSender.sendTextMessage(to, message);
        return ResponseEntity.ok(result);
    }
}
