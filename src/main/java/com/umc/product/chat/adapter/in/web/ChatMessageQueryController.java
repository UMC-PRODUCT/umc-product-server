package com.umc.product.chat.adapter.in.web;

import com.umc.product.chat.adapter.in.web.dto.response.ChatMessageResponse;
import com.umc.product.chat.adapter.in.web.dto.response.ChatRoomSummaryResponse;
import com.umc.product.chat.application.port.in.query.GetChatMessagesUseCase;
import com.umc.product.chat.application.port.in.query.GetMyChatRoomsUseCase;
import com.umc.product.chat.application.port.in.query.dto.ChatMessageCursorResult;
import com.umc.product.chat.application.port.in.query.dto.GetChatMessagesQuery;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat/rooms")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat | 채팅 메시지 Query", description = "채팅 메시지 내역 및 채팅방 목록 조회")
public class ChatMessageQueryController {

    private final GetChatMessagesUseCase getChatMessagesUseCase;
    private final GetMyChatRoomsUseCase getMyChatRoomsUseCase;

    @GetMapping("/{roomId}/messages")
    @Operation(summary = "[CHAT-002] 메시지 내역 조회", description = "방의 메시지를 최신순으로 커서 페이지네이션 조회합니다.")
    public CursorResponse<ChatMessageResponse> getMessages(
        @PathVariable @Positive Long roomId,
        @RequestParam(required = false) @Positive Long cursor,
        @RequestParam(defaultValue = "30") @Min(1) int size
    ) {
        ChatMessageCursorResult result = getChatMessagesUseCase.getMessages(
            new GetChatMessagesQuery(roomId, cursor, size));

        return CursorResponse.of(
            result.content().stream().map(ChatMessageResponse::from).toList(),
            result.nextCursor(),
            result.hasNext()
        );
    }

    @GetMapping
    @Operation(summary = "[CHAT-003] 내 채팅방 목록 조회",
        description = "내가 속한 채팅방 목록을 마지막 메시지 미리보기와 안 읽은 수와 함께 조회합니다.")
    public List<ChatRoomSummaryResponse> getMyChatRooms(@CurrentMember MemberPrincipal principal) {
        return getMyChatRoomsUseCase.getMyChatRooms(principal.getMemberId()).stream()
            .map(ChatRoomSummaryResponse::from)
            .toList();
    }
}
