package com.umc.product.report.application.port.in.command;

import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;

/**
 * 신고를 접수합니다.
 * <p>
 * 대상이 있는지, 신고자가 대상에 접근할 수 있는지 같은 대상별 검증은 호출하는 도메인이 먼저 끝내야 합니다.
 * 이 UseCase는 같은 신고자의 중복 신고만 확인하고 저장합니다.
 */
public interface SubmitReportUseCase {

    SubmitReportResult submit(SubmitReportCommand command);
}
