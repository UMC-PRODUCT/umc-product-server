package com.umc.product.community.application.port.in.command.report;

import com.umc.product.community.application.port.in.command.report.dto.ReportPostCommand;

public interface ReportPostUseCase {
    void report(ReportPostCommand command);
}
