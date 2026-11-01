package com.umc.product.report.application.port.in.command;

import com.umc.product.report.application.port.in.command.dto.ReportMemberCommand;
import com.umc.product.report.application.port.in.command.dto.ReportMemberResult;

/**
 * 다른 회원을 신고합니다.
 */
public interface ReportMemberUseCase {

    ReportMemberResult report(ReportMemberCommand command);
}
