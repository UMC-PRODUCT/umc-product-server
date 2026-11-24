package com.umc.product.notice.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.form.application.port.in.command.ManageVoteUseCase;
import com.umc.product.form.application.port.in.command.dto.CreateVoteCommand;
import com.umc.product.form.application.port.in.query.GetVoteUseCase;
import com.umc.product.form.application.port.in.query.dto.VoteInfo;
import com.umc.product.form.application.port.out.LoadFormResponsePort;
import com.umc.product.form.domain.exception.FormDomainException;
import com.umc.product.form.domain.exception.FormErrorCode;
import com.umc.product.maintenance.application.port.in.command.RefreshMaintenanceStateUseCase;
import com.umc.product.member.adapter.out.persistence.MemberSystemRoleJpaRepository;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.MemberSystemRole;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVoteRequest;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeVoteResponseUseCase;
import com.umc.product.notice.application.port.in.command.dto.SubmitNoticeVoteResponseCommand;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.application.port.out.SaveNoticeVotePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.notice.domain.enums.VoteStatus;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace;
import com.umc.product.support.concurrency.PostgreSqlTransactionRace.TransactionCall;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("공지 투표 조기 종료 통합 테스트")
class NoticeVoteCloseIntegrationTest extends IntegrationTestSupport {

    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired SchoolFixture schoolFixture;
    @Autowired SaveMemberPort saveMemberPort;
    @Autowired MemberSystemRoleJpaRepository memberSystemRoleJpaRepository;
    @Autowired EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired RefreshMaintenanceStateUseCase refreshMaintenanceStateUseCase;
    @Autowired SaveNoticePort saveNoticePort;
    @Autowired SaveNoticeTargetPort saveNoticeTargetPort;
    @Autowired SaveNoticeVotePort saveNoticeVotePort;
    @Autowired LoadNoticePort loadNoticePort;
    @Autowired LoadNoticeVotePort loadNoticeVotePort;
    @Autowired LoadFormResponsePort loadFormResponsePort;
    @Autowired ManageNoticeContentUseCase manageNoticeContentUseCase;
    @Autowired ManageNoticeVoteResponseUseCase manageNoticeVoteResponseUseCase;
    @Autowired ManageVoteUseCase manageVoteUseCase;
    @Autowired GetVoteUseCase getVoteUseCase;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbcTemplate;

    @TestBean(methodName = "fixedClock")
    Clock clock;

    private Long authorMemberId;
    private Long respondentMemberId;
    private Long noticeId;
    private List<Long> memberIds = List.of();

    static Clock fixedClock() {
        return spy(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(NOW);
        refreshMaintenanceStateUseCase.refresh();
        Long schoolId = schoolFixture.학교("투표 종료 테스트 학교").getId();
        authorMemberId = saveMember("close-author", schoolId);
        Long superAdminMemberId = saveMember("close-admin", schoolId);
        respondentMemberId = saveMember("close-other", schoolId);
        Long umcProductMemberId = saveMember("close-product", schoolId);
        memberSystemRoleJpaRepository.save(MemberSystemRole.create(superAdminMemberId, MemberSystemRoleType.SUPER_ADMIN));
        memberSystemRoleJpaRepository.save(MemberSystemRole.create(umcProductMemberId, MemberSystemRoleType.UMC_PRODUCT));
        memberIds = List.of(authorMemberId, superAdminMemberId, respondentMemberId, umcProductMemberId);
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
        setUpToken("author-token", authorMemberId);
        setUpToken("super-admin-token", superAdminMemberId);
        setUpToken("other-token", respondentMemberId);
        setUpToken("umc-product-token", umcProductMemberId);

        Notice notice = saveNoticePort.save(Notice.create("제목", "내용", authorMemberId, false, false));
        noticeId = notice.getId();
        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(noticeId)
            .targetGisuId(9L)
            .targetChallengerPart(List.of())
            .targetNoticeTab(NoticeTab.CHALLENGER)
            .build());
    }

    @AfterEach
    void clearAuthorityCache() {
        evictAuthoritySnapshotCacheUseCase.evictByMemberIds(memberIds);
    }

    @ParameterizedTest
    @ValueSource(strings = {"author-token", "super-admin-token"})
    @DisplayName("작성자와 SUPER_ADMIN은 투표를 종료하고 재조회해도 종료 상태가 유지된다")
    void 작성자_SUPER_ADMIN_조기_종료(String token) throws Exception {
        // Given
        Long voteId = createOpenVote();

        // When
        mockMvc.perform(closeRequest(token)).andExpect(status().isOk());
        Instant firstEnd = loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow().getEndsAtExclusive();
        Instant firstUpdatedAt = loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt();
        given(clock.instant()).willReturn(NOW.plusSeconds(60));
        mockMvc.perform(closeRequest(token)).andExpect(status().isOk());

        // Then
        NoticeVote closed = loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow();
        assertThat(closed.getVoteId()).isEqualTo(voteId);
        assertThat(firstEnd).isEqualTo(NOW);
        assertThat(closed.getEndsAtExclusive()).isEqualTo(firstEnd);
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(firstUpdatedAt);
        assertThat(closed.getOpenStatus(NOW)).isEqualTo(VoteStatus.CLOSED);
        mockMvc.perform(get("/api/v1/notices/{noticeId}", noticeId)
                .header("Authorization", "Bearer super-admin-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.vote.status").value("CLOSED"))
            .andExpect(jsonPath("$.result.vote.voteId").value(voteId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"other-token", "umc-product-token"})
    @DisplayName("다른 일반 계정과 UMC_PRODUCT는 403을 받고 투표 기간은 유지된다")
    void 다른_계정_조기_종료_거부(String token) throws Exception {
        // Given
        createOpenVote();
        Instant updatedAt = loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt();

        // When / Then
        mockMvc.perform(closeRequest(token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("NOTICE-0008"));
        assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow().getEndsAtExclusive())
            .isEqualTo(NOW.plusSeconds(3600));
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("종료 후 제출·수정·취소를 차단하고 응답·선택지·득표 결과를 보존한다")
    void 종료_후_투표_차단_및_결과_보존() throws Exception {
        // Given
        Long voteId = createOpenVote();
        Long optionId = getVoteUseCase.getVoteInfo(voteId, respondentMemberId).options().getFirst().optionId();
        String selectedOption = objectMapper.writeValueAsString(Map.of("optionIds", List.of(optionId)));
        mockMvc.perform(voteRequest(post(responsePath()), selectedOption)).andExpect(status().isOk());
        VoteInfo before = getVoteUseCase.getVoteInfo(voteId, respondentMemberId);
        List<Long> responseIds = loadFormResponsePort.listSubmittedByFormId(voteId).stream()
            .map(response -> response.getId()).toList();
        assertThat(before.totalParticipants()).isEqualTo(1);
        assertThat(before.mySelectedOptionIds()).containsExactly(optionId);

        // When
        mockMvc.perform(closeRequest("author-token")).andExpect(status().isOk());

        // Then
        for (MockHttpServletRequestBuilder request : List.of(
            voteRequest(post(responsePath()), selectedOption),
            voteRequest(put(responsePath()), selectedOption),
            voteRequest(put(responsePath()), "{\"optionIds\":[]}"))) {
            mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTICE-CONTENTS-0012"));
        }
        assertThat(getVoteUseCase.getVoteInfo(voteId, respondentMemberId)).isEqualTo(before);
        assertThat(loadFormResponsePort.listSubmittedByFormId(voteId))
            .extracting(response -> response.getId()).containsExactlyElementsOf(responseIds);
        mockMvc.perform(get("/api/v1/notices/{noticeId}", noticeId)
                .header("Authorization", "Bearer super-admin-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.vote.status").value("CLOSED"))
            .andExpect(jsonPath("$.result.vote.totalParticipants").value(1));
    }

    @Test
    @DisplayName("이미 종료된 투표는 반복 요청에도 마감 시각을 바꾸지 않고 성공한다")
    void 자연_종료_투표_반복_요청() throws Exception {
        // Given
        Instant pastEnd = NOW.minusSeconds(60);
        Long voteId = manageVoteUseCase.createVote(CreateVoteCommand.builder()
            .createdMemberId(authorMemberId).title("지난 투표").isAnonymous(false)
            .allowMultipleChoice(false).options(List.of("찬성", "반대")).build());
        Notice notice = loadNoticePort.findNoticeById(noticeId).orElseThrow();
        saveNoticeVotePort.saveVote(NoticeVote.create(voteId, notice, NOW.minusSeconds(3600), pastEnd));
        Instant updatedAt = notice.getUpdatedAt();

        // When / Then
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(closeRequest("author-token")).andExpect(status().isOk());
        }
        assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow().getEndsAtExclusive())
            .isEqualTo(pastEnd);
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("시작 전 투표의 종료 요청은 400이며 기간은 유지된다")
    void 시작_전_투표_종료_거부() throws Exception {
        // Given
        AddNoticeVoteRequest request = new AddNoticeVoteRequest("예정 투표", false, false,
            NOW.plusSeconds(60), NOW.plusSeconds(3600), List.of("찬성", "반대"));
        manageNoticeContentUseCase.addVote(request.toCommand(authorMemberId), noticeId);

        // When / Then
        mockMvc.perform(closeRequest("author-token"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-CONTENTS-0011"));
        assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow().getEndsAtExclusive())
            .isEqualTo(request.endsAtExclusive());
    }

    @Test
    @DisplayName("공지나 연결된 투표가 없으면 각각 404를 반환한다")
    void 공지_및_투표_없음() throws Exception {
        // When / Then
        mockMvc.perform(post("/api/v1/notices/{noticeId}/vote/closure", Long.MAX_VALUE)
                .header("Authorization", "Bearer author-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOTICE-0001"));
        mockMvc.perform(closeRequest("author-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("NOTICE-CONTENTS-0004"));
    }

    @Test
    @DisplayName("종료 중 들어온 투표 제출은 종료 커밋을 기다린 뒤 종료된 투표로 거부된다")
    void 종료와_제출_직렬화() throws Exception {
        // Given
        Long voteId = createOpenVote();
        Long optionId = getVoteUseCase.getVoteInfo(voteId, respondentMemberId).options().getFirst().optionId();
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        try (PostgreSqlTransactionRace race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            TransactionCall<Void> closure = race.submit(() -> {
                manageNoticeContentUseCase.closeVote(noticeId, authorMemberId);
                closed.countDown();
                race.await(allowCommit, "투표 종료 커밋 대기");
                return null;
            });
            try {
                race.await(closed, "투표 종료 완료");

                // When
                TransactionCall<Long> response = race.submit(() -> manageNoticeVoteResponseUseCase.submit(
                    SubmitNoticeVoteResponseCommand.builder().noticeId(noticeId)
                        .respondentMemberId(respondentMemberId).selectedOptionIds(List.of(optionId)).build()));
                try {
                    race.awaitPostgreSqlLockWait(response);
                    assertThat(response.future().isDone()).isFalse();
                } finally {
                    allowCommit.countDown();
                }
                closure.future().get(10, TimeUnit.SECONDS);

                // Then
                ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> response.future().get(10, TimeUnit.SECONDS));
                assertThat(failure.getCause()).isInstanceOf(NoticeDomainException.class)
                    .extracting(error -> ((NoticeDomainException) error).getBaseCode())
                    .isEqualTo(NoticeErrorCode.VOTE_CLOSED);
                assertThat(getVoteUseCase.getVoteInfo(voteId, respondentMemberId).totalParticipants()).isZero();
            } finally {
                allowCommit.countDown();
            }
        }
    }

    @Test
    @DisplayName("다른 사용자의 응답은 동시에 처리되고 종료는 진행 중인 응답의 커밋을 기다린다")
    void 응답_병렬_처리와_종료_대기() throws Exception {
        // Given
        Long voteId = createOpenVote();
        Long optionId = getVoteUseCase.getVoteInfo(voteId, respondentMemberId).options().getFirst().optionId();
        CountDownLatch submitted = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        try (PostgreSqlTransactionRace race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            TransactionCall<Long> first = race.submit(() -> {
                Long responseId = submitVote(respondentMemberId, optionId);
                submitted.countDown();
                race.await(allowCommit, "첫 응답 커밋 대기");
                return responseId;
            });
            try {
                race.await(submitted, "첫 응답 제출");

                // When
                TransactionCall<Long> second = race.submit(() -> submitVote(authorMemberId, optionId));
                assertThat(second.future().get(5, TimeUnit.SECONDS)).isNotNull();
                assertThat(first.future().isDone()).isFalse();

                TransactionCall<Void> closure = race.submit(() -> {
                    manageNoticeContentUseCase.closeVote(noticeId, authorMemberId);
                    return null;
                });
                race.awaitPostgreSqlLockWait(closure);
                assertThat(closure.future().isDone()).isFalse();
                allowCommit.countDown();
                assertThat(first.future().get(10, TimeUnit.SECONDS)).isNotNull();
                closure.future().get(10, TimeUnit.SECONDS);

                // Then
                assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow().getOpenStatus(NOW))
                    .isEqualTo(VoteStatus.CLOSED);
                assertThat(getVoteUseCase.getVoteInfo(voteId, respondentMemberId).totalParticipants()).isEqualTo(2);
            } finally {
                allowCommit.countDown();
            }
        }
    }

    @Test
    @DisplayName("같은 사용자의 동시 제출은 첫 응답 커밋 후 중복으로 거부되고 한 건만 저장된다")
    void 동일_사용자_동시_제출_중복_방지() throws Exception {
        // Given
        Long voteId = createOpenVote();
        Long optionId = getVoteUseCase.getVoteInfo(voteId, respondentMemberId).options().getFirst().optionId();
        CountDownLatch submitted = new CountDownLatch(1);
        CountDownLatch allowCommit = new CountDownLatch(1);
        try (PostgreSqlTransactionRace race = new PostgreSqlTransactionRace(transactionManager, jdbcTemplate)) {
            TransactionCall<Long> first = race.submit(() -> {
                Long responseId = submitVote(respondentMemberId, optionId);
                submitted.countDown();
                race.await(allowCommit, "첫 응답 커밋 대기");
                return responseId;
            });
            try {
                race.await(submitted, "첫 응답 제출");

                // When
                TransactionCall<Long> second = race.submit(() -> submitVote(respondentMemberId, optionId));
                race.awaitPostgreSqlLockWait(second);
                assertThat(second.future().isDone()).isFalse();
                allowCommit.countDown();
                assertThat(first.future().get(10, TimeUnit.SECONDS)).isNotNull();

                // Then
                ExecutionException failure = assertThrows(ExecutionException.class,
                    () -> second.future().get(10, TimeUnit.SECONDS));
                assertThat(failure.getCause()).isInstanceOf(FormDomainException.class)
                    .extracting(error -> ((FormDomainException) error).getBaseCode())
                    .isEqualTo(FormErrorCode.FORM_RESPONSE_ALREADY_EXISTS);
                assertThat(getVoteUseCase.getVoteInfo(voteId, respondentMemberId).totalParticipants()).isEqualTo(1);
            } finally {
                allowCommit.countDown();
            }
        }
    }

    private Long submitVote(Long memberId, Long optionId) {
        return manageNoticeVoteResponseUseCase.submit(SubmitNoticeVoteResponseCommand.builder()
            .noticeId(noticeId).respondentMemberId(memberId).selectedOptionIds(List.of(optionId)).build());
    }

    private Long createOpenVote() {
        return manageNoticeContentUseCase.addVote(new AddNoticeVoteRequest("투표", false, false,
            NOW.minusSeconds(60), NOW.plusSeconds(3600), List.of("찬성", "반대"))
            .toCommand(authorMemberId), noticeId).voteId();
    }

    private MockHttpServletRequestBuilder closeRequest(String token) {
        return post("/api/v1/notices/{noticeId}/vote/closure", noticeId)
            .header("Authorization", "Bearer " + token);
    }

    private String responsePath() {
        return "/api/v1/notices/" + noticeId + "/votes/responses";
    }

    private MockHttpServletRequestBuilder voteRequest(MockHttpServletRequestBuilder request, String body) {
        return request.header("Authorization", "Bearer other-token")
            .contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private Long saveMember(String name, Long schoolId) {
        return saveMemberPort.save(Member.create(name, name, name + "@test.com", schoolId, null)).getId();
    }

    private void setUpToken(String token, Long memberId) {
        given(jwtTokenProvider.validateAccessToken(token)).willReturn(true);
        given(jwtTokenProvider.parseAccessToken(token)).willReturn(memberId);
        given(jwtTokenProvider.getRolesFromAccessToken(token)).willReturn(List.of());
    }
}
