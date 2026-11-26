package com.umc.product.inquiry.application.port.in.command.dto;

import java.util.List;
import java.util.UUID;

public record SendInquiryMessageCommand(
    Long inquiryId,
    Long senderMemberId,
    String content,
    List<String> fileMetadataIds,
    UUID clientMessageId
) {
    public SendInquiryMessageCommand {
        fileMetadataIds = fileMetadataIds == null ? List.of() : List.copyOf(fileMetadataIds);
    }
}
