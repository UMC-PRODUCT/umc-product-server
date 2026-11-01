package com.umc.product.community.application.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageAdminReportInfo;
import com.umc.product.community.application.port.in.query.thread.report.dto.CommunityThreadMessageReportPageInfo;
import com.umc.product.community.application.port.in.query.thread.report.dto.SearchCommunityThreadMessageReportsQuery;
import com.umc.product.community.domain.exception.CommunityDomainException;
import com.umc.product.community.domain.exception.CommunityErrorCode;
import com.umc.product.report.application.port.in.query.SearchThreadMessageReportsUseCase;
import com.umc.product.report.application.port.in.query.dto.ReportInfo;
import com.umc.product.report.application.port.in.query.dto.SearchThreadMessageReportsQuery;
import com.umc.product.report.application.port.in.query.dto.ThreadMessageReportPageResult;
import com.umc.product.report.domain.enums.ReportReason;
import com.umc.product.report.domain.enums.ReportStatus;
import com.umc.product.report.domain.enums.ReportTargetType;

@ExtendWith(MockitoExtension.class)
@DisplayName("스레드 메시지 신고 관리자 조회 서비스")
class CommunityThreadMessageReportQueryServiceTest {

    private static final Long ADMIN_MEMBER_ID = 101L;
    private static final Long THREAD_ID = 202L;
    private static final Long REPORTER_MEMBER_ID = 303L;
    private static final Instant CREATED_AT = Instant.parse("2026-07-18T00:00:00Z");

    @Mock
    CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    @Mock
    SearchThreadMessageReportsUseCase searchThreadMessageReportsUseCase;

    @InjectMocks
    CommunityThreadMessageReportQueryService sut;

    @Test
    @DisplayName("SUPER_ADMIN만 기본 PENDING과 모든 관리자 필터를 신고 조회 UseCase에 전달하고 exact total/nextOffset을 반환한다")
    void search_SUPER_ADMIN은_기본상태와_필터_total을_전달한다() {
        // given
        SearchCommunityThreadMessageReportsQuery query = new SearchCommunityThreadMessageReportsQuery(
            ADMIN_MEMBER_ID,
            null,
            ReportReason.ABUSE,
            THREAD_ID,
            REPORTER_MEMBER_ID,
            3,
            2
        );
        SearchThreadMessageReportsQuery expectedQuery = new SearchThreadMessageReportsQuery(
            ReportStatus.PENDING,
            ReportReason.ABUSE,
            THREAD_ID,
            REPORTER_MEMBER_ID,
            3,
            2
        );
        ReportInfo first = report(91L, THREAD_ID, 701L, REPORTER_MEMBER_ID, ReportReason.ABUSE,
            ReportStatus.PENDING);
        ReportInfo second = report(90L, THREAD_ID, 700L, REPORTER_MEMBER_ID, ReportReason.ABUSE,
            ReportStatus.PENDING);
        given(checkChallengerAuthorityUseCase.isSuperAdmin(ADMIN_MEMBER_ID)).willReturn(true);
        given(searchThreadMessageReportsUseCase.search(expectedQuery))
            .willReturn(new ThreadMessageReportPageResult(List.of(first, second), 7L));
        // when
        CommunityThreadMessageReportPageInfo result = sut.search(query);

        // then
        assertThat(result.total()).isEqualTo(7L);
        assertThat(result.nextOffset()).isEqualTo(5);
        assertThat(result.items())
            .extracting(
                CommunityThreadMessageAdminReportInfo::reportId,
                CommunityThreadMessageAdminReportInfo::threadId,
                CommunityThreadMessageAdminReportInfo::messageId,
                CommunityThreadMessageAdminReportInfo::reason,
                CommunityThreadMessageAdminReportInfo::status,
                CommunityThreadMessageAdminReportInfo::reporterId,
                CommunityThreadMessageAdminReportInfo::createdAt
            )
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(
                    91L, THREAD_ID, 701L, ReportReason.ABUSE, ReportStatus.PENDING,
                    REPORTER_MEMBER_ID, CREATED_AT
                ),
                org.assertj.core.groups.Tuple.tuple(
                    90L, THREAD_ID, 700L, ReportReason.ABUSE, ReportStatus.PENDING,
                    REPORTER_MEMBER_ID, CREATED_AT
                )
            );
        then(checkChallengerAuthorityUseCase).should().isSuperAdmin(ADMIN_MEMBER_ID);
        then(searchThreadMessageReportsUseCase).should().search(expectedQuery);
    }

    @Test
    @DisplayName("SUPER_ADMIN이 아니면 신고 조회 UseCase를 호출하지 않고 접근을 거부한다")
    void search_SUPER_ADMIN이_아니면_fail_closed한다() {
        // given
        SearchCommunityThreadMessageReportsQuery query = new SearchCommunityThreadMessageReportsQuery(
            ADMIN_MEMBER_ID,
            ReportStatus.APPROVED,
            null,
            null,
            null,
            0,
            20
        );
        given(checkChallengerAuthorityUseCase.isSuperAdmin(ADMIN_MEMBER_ID)).willReturn(false);
        // when & then
        assertThatThrownBy(() -> sut.search(query))
            .isInstanceOf(CommunityDomainException.class)
            .extracting(error -> ((CommunityDomainException) error).getBaseCode())
            .isEqualTo(CommunityErrorCode.THREAD_ACCESS_DENIED);
        then(searchThreadMessageReportsUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("마지막 페이지는 offset에 결과 수를 더해도 nextOffset을 노출하지 않는다")
    void search_마지막_페이지는_nextOffset이_null이다() {
        // given
        SearchCommunityThreadMessageReportsQuery query = new SearchCommunityThreadMessageReportsQuery(
            ADMIN_MEMBER_ID,
            ReportStatus.REJECTED,
            ReportReason.ETC,
            THREAD_ID,
            null,
            4,
            2
        );
        SearchThreadMessageReportsQuery expectedQuery = new SearchThreadMessageReportsQuery(
            ReportStatus.REJECTED,
            ReportReason.ETC,
            THREAD_ID,
            null,
            4,
            2
        );
        given(checkChallengerAuthorityUseCase.isSuperAdmin(ADMIN_MEMBER_ID)).willReturn(true);
        ReportInfo report = report(80L, THREAD_ID, 600L, 404L, ReportReason.ETC, ReportStatus.REJECTED);
        given(searchThreadMessageReportsUseCase.search(expectedQuery))
            .willReturn(new ThreadMessageReportPageResult(
                List.of(report),
                5L
            ));
        // when
        CommunityThreadMessageReportPageInfo result = sut.search(query);

        // then
        assertThat(result.nextOffset()).isNull();
        assertThat(result.total()).isEqualTo(5L);
    }

    private ReportInfo report(
        Long reportId,
        Long threadId,
        Long messageId,
        Long reporterId,
        ReportReason reason,
        ReportStatus status
    ) {
        return new ReportInfo(
            reportId,
            reporterId,
            ReportTargetType.THREAD_MESSAGE,
            messageId,
            threadId,
            reason,
            List.of(reason),
            status,
            CREATED_AT
        );
    }
}
