package com.umc.product.report.application.port.in.command.dto;

import java.util.List;

import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportTargetType;

public record SubmitReportCommand(
    Long reporterId,
    ReportTargetType targetType,
    Long targetId,
    Long threadId,
    List<ReportReason> reasons,
    String detail,
    TargetSnapshot targetSnapshot
) {

    public static SubmitReportCommand of(Long reporterId, ReportTargetType targetType, Long targetId) {
        return new SubmitReportCommand(reporterId, targetType, targetId, null, null, null, null);
    }

    public static SubmitReportCommand threadMessage(
        Long reporterId,
        Long threadId,
        Long messageId,
        List<ReportReason> reasons,
        TargetSnapshot targetSnapshot
    ) {
        return new SubmitReportCommand(
            reporterId,
            ReportTargetType.THREAD_MESSAGE,
            messageId,
            threadId,
            reasons,
            null,
            targetSnapshot
        );
    }

    /**
     * 사용자 신고 명령을 만듭니다.
     *
     * @param targetMemberId 신고 대상 회원의 {@code member.id}
     * @param reasons        신고 사유 목록. 고른 순서대로 담으며 첫 번째가 대표 사유입니다.
     * @param detail         신고자가 적은 상세 내용 (필수)
     */
    public static SubmitReportCommand user(
        Long reporterId,
        Long targetMemberId,
        List<ReportReason> reasons,
        String detail,
        TargetSnapshot targetSnapshot
    ) {
        return new SubmitReportCommand(
            reporterId,
            ReportTargetType.USER,
            targetMemberId,
            null,
            reasons,
            detail,
            targetSnapshot
        );
    }

    /**
     * 신고 시점의 대상 정보입니다. 대상을 알고 있는 호출 도메인이 채웁니다.
     *
     * @param authorId 신고 대상 작성자의 {@code member.id}
     * @param content  신고 대상 내용
     * @param context  신고 대상이 있던 위치 설명 (예: 스레드 제목)
     */
    public record TargetSnapshot(Long authorId, String content, String context) {
    }
}
