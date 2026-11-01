package com.umc.product.report.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.umc.product.report.application.port.out.LoadReportPort;
import com.umc.product.report.application.port.out.SaveReportPort;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportDeliveryStatus;
import com.umc.product.report.domain.enums.ReportTargetType;
import com.umc.product.report.domain.exception.ReportDomainException;
import com.umc.product.report.domain.exception.ReportErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportPersistenceAdapter implements LoadReportPort, SaveReportPort {

    private static final String REPORTER_TARGET_DUPLICATE_CONSTRAINT =
        "uq_report_reporter_target";

    private final ReportRepository reportRepository;

    @Override
    public boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType,
                                                              Long targetId) {
        return reportRepository.existsByReporterIdAndTargetTypeAndTargetId(reporterId, targetType, targetId);
    }

    @Override
    public Optional<Report> findById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    @Override
    public Optional<Report> findByIdForUpdate(Long reportId) {
        return reportRepository.findByIdForUpdate(reportId);
    }

    @Override
    public List<Report> findRedeliveryCandidates(Instant createdBefore, int limit) {
        return reportRepository.findRedeliveryCandidates(
            createdBefore,
            ReportDeliveryStatus.FAILED,
            ReportDeliveryStatus.PENDING,
            PageRequest.of(0, limit)
        );
    }

    @Override
    public Report save(Report report) {
        try {
            return reportRepository.saveAndFlush(report);
        } catch (DataIntegrityViolationException exception) {
            if (isReporterTargetDuplicate(exception)) {
                throw new ReportDomainException(ReportErrorCode.REPORT_ALREADY_EXISTS, exception);
            }
            throw exception;
        }
    }

    private boolean isReporterTargetDuplicate(DataIntegrityViolationException exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintViolationException
                && REPORTER_TARGET_DUPLICATE_CONSTRAINT.equals(constraintViolationException.getConstraintName())) {
                return true;
            }
            if (cause.getMessage() != null
                && cause.getMessage().contains(REPORTER_TARGET_DUPLICATE_CONSTRAINT)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
