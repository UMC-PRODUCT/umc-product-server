package com.umc.product.community.application.port.out;

import com.umc.product.community.domain.Report;
import com.umc.product.community.domain.enums.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadReportPort {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);
    Page<Report> findAll(Pageable pageable);
}
