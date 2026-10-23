package com.umc.product.community.application.port.in.command.report;

import com.umc.product.community.application.port.in.command.report.dto.ReportCommentCommand;

public interface ReportCommentUseCase {
    void report(ReportCommentCommand command);
}
