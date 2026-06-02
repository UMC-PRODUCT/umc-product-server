package com.umc.product.chat.adapter.in.web.dto.request;

import com.umc.product.chat.application.port.in.command.dto.MarkChatRoomReadCommand;
import jakarta.validation.constraints.NotNull;

public record MarkChatRoomReadRequest(
    @NotNull(message = "마지막으로 읽은 메시지 ID는 필수입니다.")
    Long lastReadMessageId
) {
    public MarkChatRoomReadCommand toCommand(Long roomId, Long memberId) {
        return new MarkChatRoomReadCommand(roomId, memberId, lastReadMessageId);
    }
}
