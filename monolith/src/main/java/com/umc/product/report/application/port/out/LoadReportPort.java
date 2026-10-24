package com.umc.product.report.application.port.out;

import java.util.Optional;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportTargetType;

public interface LoadReportPort {

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    boolean existsThreadMessageReport(Long reporterId, Long messageId);

    Optional<Report> findById(Long reportId);
}
