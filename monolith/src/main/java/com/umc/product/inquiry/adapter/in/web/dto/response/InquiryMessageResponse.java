package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;
import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryAttachmentInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryMessageInfo;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;

import io.swagger.v3.oas.annotations.media.Schema;

public record InquiryMessageResponse(
    Long messageId,
    @Schema(description = "AUTHOR: 문의 작성자, STAFF: UMC PRODUCT 운영진")
    InquiryParticipant senderType,
    Long senderMemberId,
    String senderName,
    String content,
    List<Attachment> attachments,
    Instant createdAt
) {
    public static InquiryMessageResponse from(InquiryMessageInfo info) {
        return new InquiryMessageResponse(
            info.messageId(),
            info.senderType(),
            info.senderMemberId(),
            info.senderName(),
            info.content(),
            info.attachments().stream().map(Attachment::from).toList(),
            info.createdAt()
        );
    }

    public record Attachment(
        String fileId,
        String originalFileName,
        String contentType,
        Long fileSize,
        String fileLink
    ) {
        static Attachment from(InquiryAttachmentInfo info) {
            return new Attachment(
                info.fileId(), info.originalFileName(), info.contentType(), info.fileSize(), info.fileLink()
            );
        }
    }
}
