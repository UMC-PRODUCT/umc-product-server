package com.umc.product.inquiry.application.port.in.query.dto;

public record InquiryAttachmentInfo(
    String fileId,
    String originalFileName,
    String contentType,
    Long fileSize,
    String fileLink
) {
}
