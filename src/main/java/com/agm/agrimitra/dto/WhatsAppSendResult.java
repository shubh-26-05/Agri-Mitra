package com.agm.agrimitra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppSendResult {

    private boolean success;
    private String messageId;
    private String errorMessage;

    public static WhatsAppSendResult success(String messageId) {
        return WhatsAppSendResult.builder()
                .success(true)
                .messageId(messageId)
                .build();
    }

    public static WhatsAppSendResult failure(String errorMessage) {
        return WhatsAppSendResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
