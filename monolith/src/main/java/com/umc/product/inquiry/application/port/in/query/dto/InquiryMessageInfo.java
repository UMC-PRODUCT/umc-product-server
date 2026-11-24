package com.umc.product.inquiry.application.port.in.query.dto;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.domain.enums.InquiryParticipant;

/**
 * @param senderType 작성자가 보냈으면 AUTHOR, 운영진이 보냈으면 STAFF.
 */
public record InquiryMessageInfo(
    Long messageId,
    InquiryParticipant senderType,
    Long senderMemberId,
    String senderName,
    String content,
    List<InquiryAttachmentInfo> attachments,
    Instant createdAt
) {
    public InquiryMessageInfo {
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}
