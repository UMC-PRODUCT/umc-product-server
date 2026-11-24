package com.umc.product.inquiry.application.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.chat.application.port.in.command.CreateChatMessageUseCase;
import com.umc.product.chat.application.port.in.command.CreateChatRoomUseCase;
import com.umc.product.chat.application.port.in.command.JoinChatRoomUseCase;
import com.umc.product.chat.application.port.in.command.dto.ChatMessageMutationResult;
import com.umc.product.chat.application.port.in.command.dto.CreateChatMessageCommand;
import com.umc.product.chat.application.port.in.command.dto.CreateChatRoomCommand;
import com.umc.product.chat.application.port.in.command.dto.JoinChatRoomCommand;
import com.umc.product.chat.application.port.in.query.CheckChatRoomAccessUseCase;
import com.umc.product.chat.domain.ChatRoomReadScope;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.inquiry.application.event.InquiryStaffRepliedEvent;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy.InquiryAccess;
import com.umc.product.inquiry.application.policy.InquiryMessagePolicy;
import com.umc.product.inquiry.application.port.in.command.CloseInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.CreateInquiryUseCase;
import com.umc.product.inquiry.application.port.in.command.SendInquiryMessageUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.CloseInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SendInquiryMessageCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryCommandService implements CreateInquiryUseCase, SendInquiryMessageUseCase, CloseInquiryUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final CreateChatRoomUseCase createChatRoomUseCase;
    private final JoinChatRoomUseCase joinChatRoomUseCase;
    private final CheckChatRoomAccessUseCase checkChatRoomAccessUseCase;
    private final CreateChatMessageUseCase createChatMessageUseCase;
    private final InquiryAccessPolicy accessPolicy;
    private final InquiryMessagePolicy messagePolicy;
    private final InquiryMessageAssembler messageAssembler;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    @Override
    public Long create(CreateInquiryCommand command) {
        Optional<Inquiry> existing = loadInquiryPort.findByAuthorMemberIdAndClientRequestId(
            command.authorMemberId(),
            command.clientRequestId()
        );
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        InquiryCategory category = loadInquiryCategoryPort.findById(command.categoryId())
            .filter(InquiryCategory::isActive)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_CATEGORY_NOT_FOUND));
        MessageContentType contentType = messagePolicy.contentTypeOf(
            command.content(), command.fileMetadataIds(), true
        );

        Long chatRoomId = createChatRoomUseCase.create(
            CreateChatRoomCommand.of(command.authorMemberId(), ChatRoomReadScope.PUBLIC)
        ).roomId();
        Inquiry inquiry = saveInquiryPort.save(Inquiry.create(
            command.authorMemberId(),
            category.getId(),
            command.targetTeam(),
            command.title(),
            chatRoomId,
            command.clientRequestId(),
            clock.instant()
        ));

        ChatMessageMutationResult first = createChatMessageUseCase.create(
            chatMessage(chatRoomId, command.authorMemberId(), command.clientRequestId(), contentType,
                command.content(), command.fileMetadataIds()),
            messagePolicy::validateAttachments
        );
        inquiry.recordMessage(InquiryParticipant.AUTHOR, first.message().createdAt());
        saveInquiryPort.save(inquiry);
        return inquiry.getId();
    }

    @Override
    public InquiryMessageInfo send(SendInquiryMessageCommand command) {
        Inquiry inquiry = loadLockedInquiry(command.inquiryId());
        InquiryAccess access = accessPolicy.resolve(inquiry, command.senderMemberId());
        MessageContentType contentType = messagePolicy.contentTypeOf(
            command.content(), command.fileMetadataIds(), false
        );
        joinIfAbsent(inquiry.getChatRoomId(), command.senderMemberId());

        ChatMessageMutationResult result = createChatMessageUseCase.create(
            chatMessage(inquiry.getChatRoomId(), command.senderMemberId(), command.clientMessageId(), contentType,
                command.content(), command.fileMetadataIds()),
            messagePolicy::validateAttachments
        );
        InquiryMessageInfo message = messageAssembler.assemble(inquiry, result.message());
        if (result.deduplicated()) {
            return message;
        }

        inquiry.recordMessage(access.participant(), result.message().createdAt());
        saveInquiryPort.save(inquiry);
        if (access.participant() == InquiryParticipant.STAFF) {
            domainEventPublisher.publish(InquiryStaffRepliedEvent.of(
                inquiry.getId(),
                result.message().messageId(),
                inquiry.getAuthorMemberId(),
                command.senderMemberId()
            ));
        }
        return message;
    }

    @Override
    public void close(CloseInquiryCommand command) {
        accessPolicy.requireStaff(command.staffMemberId());
        Inquiry inquiry = loadLockedInquiry(command.inquiryId());
        inquiry.close(clock.instant());
        saveInquiryPort.save(inquiry);
    }

    private Inquiry loadLockedInquiry(Long inquiryId) {
        return loadInquiryPort.findByIdForUpdate(inquiryId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    // 문의 행을 잠근 뒤에만 부르므로 같은 방에 대한 동시 join 은 없다. Chat 엔진의 ALREADY_EXISTS 를 잡아 넘기면
    // 바깥 트랜잭션이 rollback-only 가 되므로, 예외를 받지 않도록 먼저 멤버인지 확인한다.
    private void joinIfAbsent(Long chatRoomId, Long memberId) {
        if (!checkChatRoomAccessUseCase.hasChatRoomAccess(memberId, chatRoomId)) {
            joinChatRoomUseCase.joinChatRoom(new JoinChatRoomCommand(chatRoomId, memberId));
        }
    }

    private CreateChatMessageCommand chatMessage(
        Long chatRoomId,
        Long senderMemberId,
        UUID clientMessageId,
        MessageContentType contentType,
        String content,
        List<String> fileMetadataIds
    ) {
        String body = content == null || content.isBlank() ? null : content;
        return new CreateChatMessageCommand(
            chatRoomId,
            senderMemberId,
            clientMessageId,
            contentType,
            body,
            fileMetadataIds,
            List.of(),
            null
        );
    }
}
