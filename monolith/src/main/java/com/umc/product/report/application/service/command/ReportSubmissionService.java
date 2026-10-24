package com.umc.product.report.application.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.report.application.port.in.command.SubmitReportUseCase;
import com.umc.product.report.application.port.in.command.dto.SubmitReportCommand;
import com.umc.product.report.application.port.in.command.dto.SubmitReportResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportSubmissionService implements SubmitReportUseCase {

    private final LoadReportPort loadReportPort;
    private final SaveReportPort saveReportPort;

    @Override
    public SubmitReportResult submit(SubmitReportCommand command) {
        if (loadReportPort.existsByReporterIdAndTargetTypeAndTargetId(
            command.reporterId(),
            command.targetType(),
            command.targetId()
        )) {
            throw new ReportDomainException(ReportErrorCode.REPORT_ALREADY_EXISTS);
        }

        // 사전 확인과 저장 사이의 동시 중복은 SaveReportPort가 REPORT_ALREADY_EXISTS로 변환합니다.
        Report savedReport = saveReportPort.save(toReport(command));
        return SubmitReportResult.from(savedReport);
    }

    private Report toReport(SubmitReportCommand command) {
        if (command.targetType() == ReportTargetType.THREAD_MESSAGE) {
            return Report.createThreadMessage(
                command.reporterId(),
                command.threadId(),
                command.targetId(),
                command.reasonCode()
            );
        }
        return Report.create(command.reporterId(), command.targetType(), command.targetId(), null);
    }
}
