package com.agm.agrimitra.service;

import com.agm.agrimitra.dto.WhatsAppSendResult;

public interface WhatsAppMessageSender {

    /**
     * Sends a plain text message to a WhatsApp user via Meta's Graph API.
     *
     * @param recipientWaId The recipient's WhatsApp ID (E.164 phone number without + or spaces)
     * @param messageText   The text message to deliver
     * @return WhatsAppSendResult containing success status, Meta message ID, or error details
     */
    WhatsAppSendResult sendTextMessage(String recipientWaId, String messageText);
}
