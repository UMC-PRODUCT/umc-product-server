package com.umc.product.report.application.service.command;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.report.application.port.in.command.ModerateReportUseCase;
import com.umc.product.report.application.port.in.command.dto.ModerateReportCommand;
import com.umc.product.report.application.port.in.command.dto.ModerateReportResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.SaveReportActionPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.ReportAction;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportModerationService implements ModerateReportUseCase {

    private final LoadReportPort loadReportPort;
    private final SaveReportPort saveReportPort;
    private final SaveReportActionPort saveReportActionPort;
    private final Clock clock;

    @Override
    public ModerateReportResult moderate(ModerateReportCommand command) {
        // 두 운영자가 동시에 눌러도 한 번만 처리되도록 신고 행을 잠그고 상태를 확인합니다.
        Report report = loadReportPort.findByIdForUpdate(command.reportId())
            .orElseThrow(() -> new ReportDomainException(ReportErrorCode.REPORT_NOT_FOUND));

        ReportAction action = report.moderate(
            command.actionType(),
            command.reason(),
            command.moderatorExternalId(),
            command.moderatorName(),
            Instant.now(clock)
        );

        saveReportPort.save(report);
        ReportAction savedAction = saveReportActionPort.save(action);
        return ModerateReportResult.of(report, savedAction);
    }
}
