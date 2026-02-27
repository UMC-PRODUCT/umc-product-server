package com.umc.product.community.application.port.out.report;

import com.umc.product.community.domain.enums.ReportTargetType;

public interface LoadReportPort {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);
}
