package com.umc.product.report.application.port.out;

import java.util.Optional;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportTargetType;

public interface LoadReportPort {

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    Optional<Report> findById(Long reportId);

    /**
     * 신고를 조회하면서 같은 신고를 동시에 처리하지 못하도록 행 잠금을 겁니다. 트랜잭션 안에서 호출해야 합니다.
     */
    Optional<Report> findByIdForUpdate(Long reportId);
}
