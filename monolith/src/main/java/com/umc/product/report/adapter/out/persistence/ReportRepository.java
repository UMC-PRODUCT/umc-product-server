package com.umc.product.report.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportTargetType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    long countByReporterIdAndTargetTypeAndTargetId(
        Long reporterId,
        ReportTargetType targetType,
        Long targetId
    );
}
