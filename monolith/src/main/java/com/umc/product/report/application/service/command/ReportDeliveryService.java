package com.umc.product.report.application.service.command;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.umc.product.report.application.port.in.command.DeliverReportUseCase;
import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.ReportDeliveryPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;

import lombok.extern.slf4j.Slf4j;

/**
 * 외부 전달 중에는 DB 트랜잭션을 잡지 않도록, 조회와 상태 저장은 각각의 저장소 호출 트랜잭션으로 처리합니다.
 */
@Slf4j
@Service
public class ReportDeliveryService implements DeliverReportUseCase {

    private final LoadReportPort loadReportPort;
    private final SaveReportPort saveReportPort;
    private final ReportDeliveryPort reportDeliveryPort;
    private final Clock clock;
    private final int maxAttempts;

    public ReportDeliveryService(
        LoadReportPort loadReportPort,
        SaveReportPort saveReportPort,
        ReportDeliveryPort reportDeliveryPort,
        Clock clock,
        @Value("${app.event-outbox.max-attempts:5}") int maxAttempts
    ) {
        this.loadReportPort = loadReportPort;
        this.saveReportPort = saveReportPort;
        this.reportDeliveryPort = reportDeliveryPort;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void deliver(Long reportId) {
        Report report = loadReportPort.findById(reportId).orElse(null);
        if (report == null) {
            log.warn("전달할 신고를 찾을 수 없어 건너뜁니다: reportId={}", reportId);
            return;
        }
        if (!report.isDeliveryPending()) {
            return;
        }
        if (!reportDeliveryPort.isEnabled()) {
            // 보내지 않은 신고를 전달 완료로 남기지 않도록 PENDING을 유지합니다.
            log.warn("신고 운영 채널이 설정되지 않아 전달하지 않습니다: reportId={}", reportId);
            return;
        }

        try {
            reportDeliveryPort.deliver(report);
        } catch (RuntimeException exception) {
            report.recordDeliveryFailure(maxAttempts);
            saveReportPort.save(report);
            log.warn(
                "신고 운영 채널 전달 실패: reportId={}, attempts={}, status={}",
                reportId,
                report.getDeliveryAttemptCount(),
                report.getDeliveryStatus()
            );
            throw exception;
        }

        report.markDelivered(Instant.now(clock));
        saveReportPort.save(report);
    }
}
