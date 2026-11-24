package com.umc.product.inquiry.application.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryAttachmentInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;

import lombok.RequiredArgsConstructor;

/**
 * Chat 메시지를 문의 메시지로 바꾼다. 보낸 사람 이름과 첨부 링크는 각각 한 번에 모아 조회한다.
 */
@Component
@RequiredArgsConstructor
class InquiryMessageAssembler {

    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;

    InquiryMessageInfo assemble(Inquiry inquiry, ChatMessageInfo message) {
        return assemble(inquiry, List.of(message)).get(0);
    }

    List<InquiryMessageInfo> assemble(Inquiry inquiry, List<ChatMessageInfo> messages) {
        if (messages.isEmpty()) {
            return List.of();
        }
        Set<Long> senderIds = new LinkedHashSet<>();
        Set<String> fileIds = new LinkedHashSet<>();
        messages.forEach(message -> {
            senderIds.add(message.senderMemberId());
            fileIds.addAll(message.fileMetadataIds());
        });
        Map<Long, String> names = getMemberUseCase.findAllNamesByIds(senderIds);
        Map<String, FileInfo> files = fileIds.isEmpty()
            ? Map.of()
            : getFileUseCase.findAllByIds(List.copyOf(fileIds));

        return messages.stream()
            .map(message -> new InquiryMessageInfo(
                message.messageId(),
                inquiry.participantOf(message.senderMemberId()),
                message.senderMemberId(),
                names.get(message.senderMemberId()),
                message.content(),
                attachments(message, files),
                message.createdAt()
            ))
            .toList();
    }

    private List<InquiryAttachmentInfo> attachments(ChatMessageInfo message, Map<String, FileInfo> files) {
        return message.fileMetadataIds().stream()
            .map(files::get)
            .filter(Objects::nonNull)
            .map(file -> new InquiryAttachmentInfo(
                file.fileId(),
                file.originalFileName(),
                file.contentType(),
                file.fileSize(),
                file.fileLink()
            ))
            .toList();
    }
}
