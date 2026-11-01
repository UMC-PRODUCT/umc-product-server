package com.umc.product.report.application.port.in.command;

import com.umc.product.report.application.port.in.command.dto.ModerateReportCommand;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;

/**
 * 운영자가 신고를 처리하거나 반려합니다.
 * <p>
 * 어느 운영 채널(Discord 등)에서 요청했는지는 알지 않습니다. 처리 대기(PENDING) 신고에만 한 번 적용되며, 처리 상태 변경과 조치 이력을 같은
 * 트랜잭션에 저장합니다.
 */
public interface ModerateReportUseCase {

    /**
     * 신고가 없으면 {@code REPORT_NOT_FOUND}, 이미 처리되었으면 {@code REPORT_ALREADY_MODERATED}로
     * {@link com.umc.product.report.domain.exception.ReportDomainException}을 던집니다.
     */
    ModerateReportResult moderate(ModerateReportCommand command);
}
