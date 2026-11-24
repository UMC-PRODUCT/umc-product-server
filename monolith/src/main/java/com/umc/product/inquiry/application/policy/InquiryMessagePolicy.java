package com.umc.product.inquiry.application.policy;

import java.util.List;

import org.springframework.stereotype.Component;

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

    /**
     * 본문과 첨부를 검사하고 Chat 엔진에 넘길 콘텐츠 종류를 정한다. 첨부가 있으면 본문은 설명으로 보고 없어도 된다.
     */
    public MessageContentType contentTypeOf(String content, List<String> fileMetadataIds, boolean contentRequired) {
        boolean blank = content == null || content.isBlank();
        if (contentRequired && blank) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_CONTENT_REQUIRED);
        }
        if (blank && fileMetadataIds.isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_MESSAGE_EMPTY);
        }
        if (!blank && content.codePointCount(0, content.length()) > MAX_CONTENT_CODE_POINTS) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_CONTENT);
        }
        if (fileMetadataIds.size() > MAX_ATTACHMENTS) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ATTACHMENT_LIMIT_EXCEEDED);
        }
        if (fileMetadataIds.stream().distinct().count() != fileMetadataIds.size()) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_ATTACHMENT);
        }
        return fileMetadataIds.isEmpty() ? MessageContentType.TEXT : MessageContentType.FILE;
    }

    /**
     * Chat 엔진이 새 메시지를 저장하기 직전에 부른다. 보낸 사람이 직접 올린 문의 첨부파일만 허용한다.
     * <p>
     * 첨부 링크는 문의 메시지 조회 응답에서만 만들어지므로, 문의 접근 정책이 곧 첨부 열람 권한이다.
     */
    public void validateAttachments(CreateChatMessageCommand command) {
        if (command.fileMetadataIds().isEmpty()) {
            return;
        }
        List<FileMetadataInfo> files = getFileUseCase.batchGetUsableByIds(
            command.fileMetadataIds(),
            command.senderMemberId()
        );
        if (files.stream().anyMatch(file -> file.category() != FileCategory.INQUIRY_ATTACHMENT)) {
            throw new InquiryDomainException(InquiryErrorCode.INVALID_INQUIRY_ATTACHMENT);
        }
    }
}
