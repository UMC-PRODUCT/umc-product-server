package com.umc.product.chat.application.port.in.command.dto;

public record MarkChatRoomReadCommand(
    Long roomId,
    Long memberId,
    Long lastReadMessageId
) {
}
