package com.umc.product.chat.application.port.in.command.dto;

import com.umc.product.chat.domain.MessageContentType;

/**
 * 채팅방 퇴장 커맨드.
 *
 * @param exitMessageType 퇴장 기록 메시지 타입({@code MEMBER_LEFT}/{@code MEMBER_KICKED}). null이면 기록을 남기지 않는다.
 */
public record LeaveChatRoomCommand(
    Long roomId,
    Long memberId,
    MessageContentType exitMessageType
) {
    public LeaveChatRoomCommand {
        if (exitMessageType != null && !exitMessageType.isMemberExit()) {
            throw new IllegalArgumentException("exitMessageType must be MEMBER_LEFT or MEMBER_KICKED");
        }
    }

    public static LeaveChatRoomCommand of(Long roomId, Long memberId) {
        return new LeaveChatRoomCommand(roomId, memberId, null);
    }

    public static LeaveChatRoomCommand left(Long roomId, Long memberId) {
        return new LeaveChatRoomCommand(roomId, memberId, MessageContentType.MEMBER_LEFT);
    }

    public static LeaveChatRoomCommand kicked(Long roomId, Long memberId) {
        return new LeaveChatRoomCommand(roomId, memberId, MessageContentType.MEMBER_KICKED);
    }
}
