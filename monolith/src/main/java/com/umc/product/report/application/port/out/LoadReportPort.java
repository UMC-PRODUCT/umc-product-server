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
     * {@code createdBefore} 이전에 접수된 신고 중 다시 보낼 대상을 오래된 순으로 최대 {@code limit}건 조회합니다.
     * <p>
     * 실패(FAILED)했거나 한 번도 시도하지 않은 대기(PENDING, 실패 횟수 0) 신고만 대상입니다. 재시도 중인 신고(PENDING, 실패 횟수 1 이상)는
     * outbox가 아직 처리하고 있으므로 넣지 않습니다.
     */
    List<Report> findRedeliveryCandidates(Instant createdBefore, int limit);
}
