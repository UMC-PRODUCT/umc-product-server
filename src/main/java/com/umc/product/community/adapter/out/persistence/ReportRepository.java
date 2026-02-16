package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.domain.Report;
import com.umc.product.community.domain.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);
}
