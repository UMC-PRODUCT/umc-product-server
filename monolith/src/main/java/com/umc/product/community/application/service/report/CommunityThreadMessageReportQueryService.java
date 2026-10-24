package com.umc.product.community.application.service.report;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.community.application.port.in.query.thread.report.SearchCommunityThreadMessageReportsUseCase;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageAdminReportInfo;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageReportPageInfo;
import com.umc.product.community.application.port.in.query.thread.report.dto.SearchCommunityThreadMessageReportsQuery;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.report.application.port.in.query.SearchThreadMessageReportsUseCase;
import com.umc.product.report.application.port.in.query.dto.SearchThreadMessageReportsQuery;
import com.umc.product.report.application.port.in.query.dto.ThreadMessageReportPageResult;
import com.umc.product.report.domain.enums.ReportStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityThreadMessageReportQueryService implements SearchCommunityThreadMessageReportsUseCase {

    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;
    private final SearchThreadMessageReportsUseCase searchThreadMessageReportsUseCase;

    @Override
    public CommunityThreadMessageReportPageInfo search(SearchCommunityThreadMessageReportsQuery query) {
        validatePage(query.offset(), query.limit());
        if (query.requesterMemberId() == null
            || !checkChallengerAuthorityUseCase.isSuperAdmin(query.requesterMemberId())) {
            throw new CommunityDomainException(CommunityErrorCode.THREAD_ACCESS_DENIED);
        }

        ThreadMessageReportPageResult result = searchThreadMessageReportsUseCase.search(
            new SearchThreadMessageReportsQuery(
                query.status() == null ? ReportStatus.PENDING : query.status(),
                query.reason(),
                query.threadId(),
                query.reporterMemberId(),
                query.offset(),
                query.limit()
            )
        );
        List<CommunityThreadMessageAdminReportInfo> items = result.reports().stream()
            .map(CommunityThreadMessageAdminReportInfo::from)
            .toList();
        long candidateNextOffset = (long) query.offset() + items.size();
        Integer nextOffset = candidateNextOffset < result.total()
            ? Math.toIntExact(candidateNextOffset)
            : null;
        return new CommunityThreadMessageReportPageInfo(items, nextOffset, result.total());
    }

    private void validatePage(int offset, int limit) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
    }
}
