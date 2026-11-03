package com.umc.product.report.application.port.in.command.dto;

import com.umc.product.report.domain.enums.ReportActionReason;
import com.umc.product.report.domain.enums.ReportActionType;

/**
 * 신고 조치 요청입니다.
 *
 * @param reportId            조치할 신고 ID
 * @param actionType          처리 또는 반려
 * @param reasonCode          조치에 맞는 사유 항목
 * @param memo                운영자 메모 (최대 500자). 기타 사유면 필수입니다.
 * @param moderatorExternalId 조치자의 운영 채널 사용자 ID
 * @param moderatorName       조치자의 표시 이름
 */
public record ModerateReportCommand(
    Long reportId,
    ReportActionType actionType,
    ReportActionReason reasonCode,
    String memo,
    String moderatorExternalId,
    String moderatorName
) {
}
