package com.umc.product.inquiry.application.port.in.command.dto;

import java.util.List;
import java.util.UUID;

import com.umc.product.inquiry.domain.enums.InquiryTeam;

public record CreateInquiryCommand(
    Long authorMemberId,
    Long categoryId,
    InquiryTeam targetTeam,
    String title,
    String content,
    List<String> fileMetadataIds,
    UUID clientRequestId
) {
    public CreateInquiryCommand {
        fileMetadataIds = fileMetadataIds == null ? List.of() : List.copyOf(fileMetadataIds);
    }
}
