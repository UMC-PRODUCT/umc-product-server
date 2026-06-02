package com.umc.product.chat.application.service.command;

import com.umc.product.chat.application.port.in.command.MarkChatRoomReadUseCase;
import com.umc.product.chat.application.port.in.command.SendChatMessageUseCase;
import com.umc.product.chat.application.port.in.command.dto.MarkChatRoomReadCommand;
import com.umc.product.chat.application.port.in.command.dto.SendChatMessageCommand;
import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.chat.application.port.out.LoadChatMemberPort;
import com.umc.product.chat.application.port.out.SaveChatMemberPort;
import com.umc.product.chat.application.port.out.SaveChatMessagePort;
import com.umc.product.chat.domain.ChatMember;
import com.umc.product.chat.domain.ChatMessage;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.chat.domain.event.ChatMessageCreatedEvent;
import com.umc.product.chat.domain.exception.ChatDomainException;
import com.umc.product.chat.domain.exception.ChatErrorCode;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageCommandService implements SendChatMessageUseCase, MarkChatRoomReadUseCase {

    private final SaveChatMessagePort saveChatMessagePort;
    private final LoadChatMemberPort loadChatMemberPort;
    private final SaveChatMemberPort saveChatMemberPort;
    private final DomainEventPublisher domainEventPublisher;

    /**
     * 메시지를 저장하고 생성 이벤트를 발행한다.
     * <p>
     * broadcast 및 문의 상태 전환은 이 이벤트를 수신하는 다른 컴포넌트가 처리한다. chat은 알지 못한다.
     * 이벤트 발행은 {@link DomainEventPublisher} 한 곳에만 위임하며, outbox 적재 / 인메모리 발행 분기는
     * 어댑터 구성(app.event-outbox.enabled)이 결정한다.
     */
    @Override
    public ChatMessageInfo send(SendChatMessageCommand command) {
        validate(command);

        // SEND 권한 인터셉터 도입 전 임시 가드: 방 멤버만 전송 가능
        if (!loadChatMemberPort.existsByRoomIdAndMemberId(command.roomId(), command.senderMemberId())) {
            throw new ChatDomainException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND);
        }

        ChatMessage saved = saveChatMessagePort.save(ChatMessage.create(
            command.roomId(),
            command.senderMemberId(),
            command.contentType(),
            command.content(),
            command.fileMetadataIds()
        ));

        domainEventPublisher.publish(ChatMessageCreatedEvent.from(saved));

        return ChatMessageInfo.from(saved);
    }

    @Override
    public void markRead(MarkChatRoomReadCommand command) {
        ChatMember member = loadChatMemberPort.getByRoomIdAndMemberId(command.roomId(), command.memberId());
        member.markRead(command.lastReadMessageId());
        saveChatMemberPort.save(member);
    }

    private void validate(SendChatMessageCommand command) {
        // SYSTEM 메시지는 서버 내부에서만 생성한다(클라이언트 전송 불가).
        if (command.contentType() == MessageContentType.SYSTEM) {
            throw new ChatDomainException(ChatErrorCode.CHAT_MESSAGE_INVALID_CONTENT_TYPE);
        }
        boolean noContent = command.content() == null || command.content().isBlank();
        boolean noFiles = command.fileMetadataIds() == null || command.fileMetadataIds().isEmpty();
        if (noContent && noFiles) {
            throw new ChatDomainException(ChatErrorCode.CHAT_MESSAGE_EMPTY);
        }
    }
}
