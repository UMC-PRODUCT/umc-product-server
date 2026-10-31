package com.umc.product.community.application.port.in.command.report.dto;

import java.util.Objects;

import com.umc.product.report.domain.enums.ReportReason;

/**
 * 사용자 신고 명령입니다.
 *
 * @param targetMemberId    신고 대상 회원의 {@code member.id}
 * @param requesterMemberId 신고자의 {@code member.id}
 * @param reason            신고 사유
 * @param detail            상세 신고 내용
 */
public record ReportCommunityMemberCommand(
    Long targetMemberId,
    Long requesterMemberId,
    ReportReason reason,
    String detail
) {

    public ReportCommunityMemberCommand {
        targetMemberId = requirePositive(targetMemberId, "targetMemberId");
        requesterMemberId = requirePositive(requesterMemberId, "requesterMemberId");
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(detail, "detail must not be null");
    }

    private static Long requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
