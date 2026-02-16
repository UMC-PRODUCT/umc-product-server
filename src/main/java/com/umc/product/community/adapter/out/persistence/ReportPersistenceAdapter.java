package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.out.LoadReportPort;
import com.umc.product.community.application.port.out.SaveReportPort;
import com.umc.product.community.domain.Report;
import com.umc.product.community.domain.enums.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements LoadReportPort, SaveReportPort {

    private final ReportRepository reportRepository;

    @Override
    public boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId) {
        return reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    @Override
    public Report save(Report report) {
        return reportRepository.save(report);
    }
}
