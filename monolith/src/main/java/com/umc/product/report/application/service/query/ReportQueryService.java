package com.umc.product.report.application.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.report.application.port.in.query.SearchThreadMessageReportsUseCase;
import com.umc.product.report.application.port.in.query.dto.ReportInfo;
import com.umc.product.report.application.port.in.query.dto.SearchThreadMessageReportsQuery;
import com.umc.product.report.application.port.in.query.dto.ThreadMessageReportPageResult;
import com.umc.product.report.application.port.out.SearchThreadMessageReportPort;
import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchQuery;
import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService implements SearchThreadMessageReportsUseCase {

    private final SearchThreadMessageReportPort searchThreadMessageReportPort;

    @Override
    public ThreadMessageReportPageResult search(SearchThreadMessageReportsQuery query) {
        ThreadMessageReportSearchResult result = searchThreadMessageReportPort.search(
            new ThreadMessageReportSearchQuery(
                query.status(),
                query.reason(),
                query.threadId(),
                query.reporterId(),
                query.offset(),
                query.limit()
            )
        );
        return new ThreadMessageReportPageResult(
            result.reports().stream().map(ReportInfo::from).toList(),
            result.total()
        );
    }
}
