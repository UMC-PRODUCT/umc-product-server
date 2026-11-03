package com.umc.product.report.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.report.domain.ReportAction;

public interface ReportActionRepository extends JpaRepository<ReportAction, Long> {

    List<ReportAction> findAllByReportIdOrderByActedAtAsc(Long reportId);
}
