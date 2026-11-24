package com.umc.product.inquiry.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryAuthorInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.domain.enums.InquiryStatus;
import com.umc.product.inquiry.domain.enums.InquiryTeam;

public record InquiryDetailResponse(
    Long inquiryId,
    String title,
    InquiryCategoryResponse category,
    InquiryTeam targetTeam,
    InquiryStatus status,
    Author author,
    Instant createdAt,
    Instant closedAt,
    Instant lastMessageAt,
    boolean canSendMessage,
    boolean canClose
) {
    public static InquiryDetailResponse from(InquiryDetailInfo info) {
        return new InquiryDetailResponse(
            info.inquiryId(),
            info.title(),
            InquiryCategoryResponse.from(info.category()),
            info.targetTeam(),
            info.status(),
            Author.from(info.author()),
            info.createdAt(),
            info.closedAt(),
            info.lastMessageAt(),
            info.canSendMessage(),
            info.canClose()
        );
    }

    public record Author(
        Long memberId,
        String name,
        String nickname,
        String profileImageUrl
    ) {
        static Author from(InquiryAuthorInfo info) {
            return new Author(info.memberId(), info.name(), info.nickname(), info.profileImageUrl());
        }
    }
}
