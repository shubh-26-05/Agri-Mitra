package com.agm.agrimitra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsAppSendMessageDto {

    @JsonProperty("messaging_product")
    @Builder.Default
    private String messagingProduct = "whatsapp";

    private String to;

    @Builder.Default
    private String type = "text";

    private TextContent text;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent {
        private String body;
    }

    public static WhatsAppSendMessageDto create(String to, String messageText) {
        return WhatsAppSendMessageDto.builder()
                .messagingProduct("whatsapp")
                .to(to)
                .type("text")
                .text(new TextContent(messageText))
                .build();
    }
}
