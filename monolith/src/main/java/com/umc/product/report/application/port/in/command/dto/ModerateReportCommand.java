package com.umc.product.report.application.port.in.command.dto;

import com.umc.product.report.domain.enums.ReportActionType;

/**
 * 신고 조치 요청입니다.
 *
 * @param reportId            조치할 신고 ID
 * @param actionType          처리 또는 반려
 * @param reason              조치 사유 (최대 500자)
 * @param moderatorExternalId 조치자의 운영 채널 사용자 ID
 * @param moderatorName       조치자의 표시 이름
 */
public record ModerateReportCommand(
    Long reportId,
    ReportActionType actionType,
    String reason,
    String moderatorExternalId,
    String moderatorName
) {
}
