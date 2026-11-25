package com.umc.product.inquiry.application.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.umc.product.chat.application.port.in.query.dto.ChatMessageInfo;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryAttachmentInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.storage.application.port.in.query.GetFileUseCase;
import com.umc.product.storage.application.port.in.query.dto.FileInfo;

import lombok.RequiredArgsConstructor;

/**
 * 문의 메시지를 보는 사람에 맞춰 만든다. 운영진은 누가 보냈는지 실명으로 보고, 운영진이 아닌 사람에게는 운영진 메시지를
 * {@value #STAFF_DISPLAY_NAME} 한 이름으로만 보인다.
 */
@Component
@RequiredArgsConstructor
class InquiryMessageAssembler {

    static final String STAFF_DISPLAY_NAME = "UMC PRODUCT";

    private final GetMemberUseCase getMemberUseCase;
    private final GetFileUseCase getFileUseCase;
    private final InquiryAccessPolicy accessPolicy;

    InquiryMessageInfo assemble(Inquiry inquiry, ChatMessageInfo message, Long viewerMemberId) {
        return assemble(inquiry, List.of(message), viewerMemberId).get(0);
    }

    List<InquiryMessageInfo> assemble(Inquiry inquiry, List<ChatMessageInfo> messages, Long viewerMemberId) {
        List<InquiryMessageInfo> staffViews = staffViews(inquiry, messages);
        if (seesStaffIdentity(viewerMemberId)) {
            return staffViews;
        }
        return staffViews.stream().map(InquiryMessageAssembler::hideStaffIdentity).toList();
    }

    /**
     * 실시간처럼 여러 사람에게 한 메시지를 보낼 때 쓴다. 첨부 링크를 한 번만 만들고 두 가지 모양을 함께 돌려준다.
     */
    Views assembleViews(Inquiry inquiry, ChatMessageInfo message) {
        InquiryMessageInfo staffView = staffViews(inquiry, List.of(message)).get(0);
        return new Views(staffView, hideStaffIdentity(staffView));
    }

    boolean seesStaffIdentity(Long viewerMemberId) {
        return accessPolicy.isStaff(viewerMemberId);
    }

    private List<InquiryMessageInfo> staffViews(Inquiry inquiry, List<ChatMessageInfo> messages) {
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

    private static InquiryMessageInfo hideStaffIdentity(InquiryMessageInfo message) {
        if (message.senderType() != InquiryParticipant.STAFF) {
            return message;
        }
        return new InquiryMessageInfo(
            message.messageId(),
            message.senderType(),
            null,
            STAFF_DISPLAY_NAME,
            message.content(),
            message.attachments(),
            message.createdAt()
        );
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

    record Views(InquiryMessageInfo staff, InquiryMessageInfo requester) {

        InquiryMessageInfo forViewer(boolean seesStaffIdentity) {
            return seesStaffIdentity ? staff : requester;
        }
    }
}
