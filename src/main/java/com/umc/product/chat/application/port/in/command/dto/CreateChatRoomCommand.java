package com.umc.product.chat.application.port.in.command.dto;

public record CreateChatRoomCommand(
    Long creatorMemberId
) {
}
