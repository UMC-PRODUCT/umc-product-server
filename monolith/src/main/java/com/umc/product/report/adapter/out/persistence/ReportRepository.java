package com.umc.product.report.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

import jakarta.persistence.LockModeType;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    long countByReporterIdAndTargetTypeAndTargetId(
        Long reporterId,
        ReportTargetType targetType,
        Long targetId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Report r where r.id = :reportId")
    Optional<Report> findByIdForUpdate(@Param("reportId") Long reportId);

    @Query("""
        select r from Report r
        where r.createdAt < :createdBefore
          and (r.deliveryStatus = :failed
            or (r.deliveryStatus = :pending and r.deliveryAttemptCount = 0))
        order by r.id asc
        """)
    List<Report> findRedeliveryCandidates(
        @Param("createdBefore") Instant createdBefore,
        @Param("failed") ReportDeliveryStatus failed,
        @Param("pending") ReportDeliveryStatus pending,
        Pageable pageable
    );
}
