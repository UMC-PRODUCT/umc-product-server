package com.umc.product.chat.application.port.in.query.dto;

public record GetChatMessagesQuery(
    Long roomId,
    Long cursorId,
    int size
) {
}
