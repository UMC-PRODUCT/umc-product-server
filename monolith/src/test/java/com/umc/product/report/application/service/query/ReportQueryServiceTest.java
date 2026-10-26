package com.umc.product.report.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.report.application.port.in.query.dto.ReportInfo;
import com.umc.product.report.application.port.in.query.dto.SearchThreadMessageReportsQuery;
import com.umc.product.report.application.port.in.query.dto.ThreadMessageReportPageResult;
import com.umc.product.report.application.port.out.SearchThreadMessageReportPort;
import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchQuery;
import com.umc.product.report.application.port.out.dto.ThreadMessageReportSearchResult;
import com.umc.product.report.domain.Report;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

@ExtendWith(MockitoExtension.class)
@DisplayName("신고 조회 서비스")
class ReportQueryServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-07-18T00:00:00Z");

    @Mock
    SearchThreadMessageReportPort searchThreadMessageReportPort;

    @InjectMocks
    ReportQueryService sut;

    @Test
    @DisplayName("검색 조건을 그대로 조회 포트에 전달하고 신고 엔티티를 신고 정보로 바꿔 돌려준다")
    void search_조건을_전달하고_신고_정보로_돌려준다() {
        // given
        Report report = report();
        given(searchThreadMessageReportPort.search(
            new ThreadMessageReportSearchQuery(ReportStatus.PENDING, ReportReason.ABUSE, 202L, 303L, 3, 2)
        )).willReturn(new ThreadMessageReportSearchResult(List.of(report), 7L));

        // when
        ThreadMessageReportPageResult result = sut.search(
            new SearchThreadMessageReportsQuery(ReportStatus.PENDING, ReportReason.ABUSE, 202L, 303L, 3, 2)
        );

        // then
        assertThat(result.total()).isEqualTo(7L);
        assertThat(result.reports()).containsExactly(new ReportInfo(
            91L,
            303L,
            ReportTargetType.THREAD_MESSAGE,
            701L,
            202L,
            ReportReason.ABUSE,
            ReportStatus.PENDING,
            CREATED_AT
        ));
    }

    private Report report() {
        Report report = mock(Report.class);
        given(report.getId()).willReturn(91L);
        given(report.getReporterId()).willReturn(303L);
        given(report.getTargetType()).willReturn(ReportTargetType.THREAD_MESSAGE);
        given(report.getTargetId()).willReturn(701L);
        given(report.getThreadId()).willReturn(202L);
        given(report.getReasonCode()).willReturn(ReportReason.ABUSE);
        given(report.getStatus()).willReturn(ReportStatus.PENDING);
        given(report.getCreatedAt()).willReturn(CREATED_AT);
        return report;
    }
}
