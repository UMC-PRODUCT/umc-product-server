package com.umc.product.chat.adapter.in.web;

import com.umc.product.chat.adapter.in.web.dto.request.MarkChatRoomReadRequest;
import com.umc.product.chat.application.port.in.command.MarkChatRoomReadUseCase;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat | 채팅 메시지 Command", description = "읽음 처리")
public class ChatMessageCommandController {

    private final MarkChatRoomReadUseCase markChatRoomReadUseCase;

    @PatchMapping("/{roomId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "[CHAT-103] 읽음 처리", description = "채팅방을 지정한 메시지까지 읽음 처리합니다.")
    public void markRead(
        @PathVariable Long roomId,
        @Valid @RequestBody MarkChatRoomReadRequest request,
        @CurrentMember MemberPrincipal principal
    ) {
        markChatRoomReadUseCase.markRead(request.toCommand(roomId, principal.getMemberId()));
    }
}
