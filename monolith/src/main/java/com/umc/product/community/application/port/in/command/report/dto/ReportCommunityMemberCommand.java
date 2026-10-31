package com.umc.product.community.application.port.in.command.report.dto;

import java.util.List;
import java.util.Objects;

import com.umc.product.report.domain.enums.ReportReason;

/**
 * 사용자 신고 명령입니다.
 *
 * @param targetMemberId    신고 대상 회원의 {@code member.id}
 * @param requesterMemberId 신고자의 {@code member.id}
 * @param reasons           신고 사유 목록 (고른 순서대로, 최소 1개)
 * @param detail            상세 신고 내용
 */
public record ReportCommunityMemberCommand(
    Long targetMemberId,
    Long requesterMemberId,
    List<ReportReason> reasons,
    String detail
) {

    public ReportCommunityMemberCommand {
        targetMemberId = requirePositive(targetMemberId, "targetMemberId");
        requesterMemberId = requirePositive(requesterMemberId, "requesterMemberId");
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("reasons must not be empty");
        }
        reasons = List.copyOf(reasons);
        Objects.requireNonNull(detail, "detail must not be null");
    }

    private static Long requirePositive(Long value, String name) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
