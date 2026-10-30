package com.umc.product.report.adapter.out.persistence;

import org.springframework.stereotype.Component;

import com.umc.product.report.application.port.out.SaveReportActionPort;
import com.umc.product.report.domain.ReportAction;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReportActionPersistenceAdapter implements SaveReportActionPort {

    private final ReportActionRepository reportActionRepository;

    @Override
    public ReportAction save(ReportAction action) {
        return reportActionRepository.save(action);
    }
}
