package com.umc.product.report.application.port.out;

import java.time.Instant;
import java.util.List;
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

    /**
     * {@code createdBefore} 이전에 접수되어 아직 전달하지 못한(PENDING, FAILED) 신고를 오래된 순으로 최대 {@code limit}건 조회합니다.
     */
    List<Report> findUndeliveredCreatedBefore(Instant createdBefore, int limit);
}
