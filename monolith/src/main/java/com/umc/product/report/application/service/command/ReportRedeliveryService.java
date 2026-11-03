package com.umc.product.report.application.service.command;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.report.application.port.in.command.RedeliverReportsUseCase;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsCommand;
import com.umc.product.report.application.port.in.command.dto.RedeliverReportsResult;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.event.ReportSubmittedEvent;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportRedeliveryService implements RedeliverReportsUseCase {

    /** 접수 직후 첫 전달이 진행 중인 신고를 다시 넣지 않도록 기다리는 시간입니다. */
    static final Duration IN_FLIGHT_GRACE = Duration.ofMinutes(5);

    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    private final LoadReportPort loadReportPort;
    private final SaveReportPort saveReportPort;
    private final ReportDeliveryPort reportDeliveryPort;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    @Override
    public RedeliverReportsResult redeliver(RedeliverReportsCommand command) {
        if (command.requesterMemberId() == null
            || !checkChallengerAuthorityUseCase.isSuperAdmin(command.requesterMemberId())) {
            throw new ReportDomainException(ReportErrorCode.REPORT_ADMIN_REQUIRED);
        }
        if (!reportDeliveryPort.isEnabled()) {
            // 설정 없이 다시 넣으면 PENDING으로 남을 뿐이므로, 다시 보낼 수 없다고 바로 알려 줍니다.
            throw new ReportDomainException(ReportErrorCode.REPORT_DELIVERY_DISABLED);
        }

        Instant createdBefore = Instant.now(clock).minus(IN_FLIGHT_GRACE);
        List<Report> reports = loadReportPort.findRedeliveryCandidates(createdBefore, command.limit());
        for (Report report : reports) {
            report.requeueDelivery();
            saveReportPort.save(report);
        }
        // 접수 때와 같은 이벤트를 다시 넣어, 같은 전달 경로(outbox relay → ReportDeliveryService)로 보냅니다.
        domainEventPublisher.publishAll(reports.stream()
            .map(report -> ReportSubmittedEvent.of(report.getId()))
            .toList());
        return new RedeliverReportsResult(reports.stream().map(Report::getId).toList());
    }
}
