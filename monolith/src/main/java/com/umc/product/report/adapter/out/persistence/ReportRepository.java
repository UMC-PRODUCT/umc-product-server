package com.umc.product.report.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.report.domain.Report;
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
}
