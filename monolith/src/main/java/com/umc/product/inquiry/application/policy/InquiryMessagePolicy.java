package com.umc.product.inquiry.application.policy;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.umc.product.chat.application.port.in.command.ValidateChatAttachmentsUseCase;
import com.umc.product.chat.application.port.in.command.dto.CreateChatMessageCommand;
import com.umc.product.chat.domain.MessageContentType;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileMetadataInfo;
import com.umc.product.storage.domain.enums.FileCategory;

import lombok.RequiredArgsConstructor;

/**
 * 문의 메시지의 본문·첨부 규칙. 검증을 마친 Chat 메시지 명령만 돌려준다.
 */
@Component
@RequiredArgsConstructor
public class InquiryMessagePolicy {

    private static final int MAX_CONTENT_CODE_POINTS = 2_000;
    private static final int MAX_ATTACHMENTS = 5;

    private final GetFileUseCase getFileUseCase;
    private final ValidateChatAttachmentsUseCase validateChatAttachmentsUseCase;

    /**
     * 문의 등록의 첫 메시지. 본문이 곧 문의 내용이라 첨부가 있어도 비울 수 없다.
     */
    public CreateChatMessageCommand openingMessage(
        Long chatRoomId,
        Long senderMemberId,
        UUID clientMessageId,
        String content,
        List<String> fileMetadataIds
    ) {
        if (isBlank(content)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_CONTENT_REQUIRED);
        }
        return message(chatRoomId, senderMemberId, clientMessageId, content, fileMetadataIds);
    }

    /**
     * 등록 뒤 대화 메시지. 첨부가 있으면 본문은 설명이라 비워도 된다.
     */
    public CreateChatMessageCommand reply(
        Long chatRoomId,
        Long senderMemberId,
        UUID clientMessageId,
        String content,
        List<String> fileMetadataIds
    ) {
        if (isBlank(content) && fileMetadataIds.isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_MESSAGE_EMPTY);
        }
        return message(chatRoomId, senderMemberId, clientMessageId, content, fileMetadataIds);
    }

    private CreateChatMessageCommand message(
        Long chatRoomId,
        Long senderMemberId,
        UUID clientMessageId,
        String content,
        List<String> fileMetadataIds
    ) {
        String body = isBlank(content) ? null : content;
        if (body != null && body.codePointCount(0, body.length()) > MAX_CONTENT_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_CONTENT);
        }
        return new CreateChatMessageCommand(
            chatRoomId,
            senderMemberId,
            clientMessageId,
            contentTypeOf(senderMemberId, fileMetadataIds),
            body,
            fileMetadataIds,
            List.of(),
            null
        );
    }

    /**
     * 첨부 구성으로 Chat 콘텐츠 종류를 정한다. 한 메시지는 한 종류만 담을 수 있어 이미지와 문서를 섞으면 거절한다.
     */
    private MessageContentType contentTypeOf(Long senderMemberId, List<String> fileMetadataIds) {
        if (fileMetadataIds.isEmpty()) {
            return MessageContentType.TEXT;
        }
        if (fileMetadataIds.size() > MAX_ATTACHMENTS) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ATTACHMENT_LIMIT_EXCEEDED);
        }
        if (fileMetadataIds.stream().distinct().count() != fileMetadataIds.size()) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_ATTACHMENT);
        }

        List<FileMetadataInfo> files = getFileUseCase.batchGetUsableByIds(fileMetadataIds, senderMemberId);
        long images = files.stream().filter(InquiryMessagePolicy::isImage).count();
        if (images > 0 && images < files.size()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ATTACHMENT_MIXED_TYPES);
        }
        MessageContentType contentType = images > 0 ? MessageContentType.IMAGE : MessageContentType.FILE;
        validateChatAttachmentsUseCase.validate(contentType, files);
        if (files.stream().anyMatch(file -> file.category() != FileCategory.INQUIRY_ATTACHMENT)) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_ATTACHMENT);
        }
        return contentType;
    }

    private static boolean isImage(FileMetadataInfo file) {
        return file.contentType() != null && file.contentType().toLowerCase(Locale.ROOT).startsWith("image/");
    }

    private static boolean isBlank(String content) {
        return content == null || content.isBlank();
    }
}
