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

@Component
@RequiredArgsConstructor
public class InquiryMessagePolicy {

    private static final int MAX_CONTENT_CODE_POINTS = 2_000;
    private static final int MAX_ATTACHMENTS = 5;

    private final GetFileUseCase getFileUseCase;
    private final ValidateChatAttachmentsUseCase validateChatAttachmentsUseCase;

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
