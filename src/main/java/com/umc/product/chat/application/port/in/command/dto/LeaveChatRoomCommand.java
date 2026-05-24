package com.umc.product.chat.application.port.in.command.dto;

public record LeaveChatRoomCommand(
    Long roomId,
    Long memberId
) {
}
