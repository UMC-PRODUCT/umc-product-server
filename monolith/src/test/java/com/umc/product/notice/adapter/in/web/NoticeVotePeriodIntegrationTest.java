package com.umc.product.notice.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.form.application.port.out.LoadFormPort;
import com.umc.product.member.application.port.out.SaveMemberPort;
import com.umc.product.member.domain.Member;
import com.umc.product.notice.adapter.in.web.dto.request.AddNoticeVoteRequest;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeVotePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.NoticeVote;
import com.umc.product.notice.domain.enums.NoticeTab;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.SchoolFixture;

@DisplayName("공지 투표 등록 기간 검증")
class NoticeVotePeriodIntegrationTest extends IntegrationTestSupport {

    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired
    SchoolFixture schoolFixture;
    @Autowired
    SaveMemberPort saveMemberPort;
    @Autowired
    EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @Autowired
    SaveNoticePort saveNoticePort;
    @Autowired
    SaveNoticeTargetPort saveNoticeTargetPort;
    @Autowired
    LoadNoticePort loadNoticePort;
    @Autowired
    LoadNoticeVotePort loadNoticeVotePort;
    @Autowired
    LoadFormPort loadFormPort;

    @TestBean(methodName = "fixedClock")
    Clock clock;

    private Long authorMemberId;
    private Long noticeId;

    static Clock fixedClock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    @BeforeEach
    void setUp() {
        Long schoolId = schoolFixture.학교("공지 테스트 학교").getId();
        authorMemberId = saveMemberPort.save(Member.create(
            "공지 작성자", "공지작성자", "vote-author@test.com", schoolId, null)).getId();
        evictAuthoritySnapshotCacheUseCase.evictByMemberId(authorMemberId);
        given(jwtTokenProvider.validateAccessToken("vote-period-token")).willReturn(true);
        given(jwtTokenProvider.parseAccessToken("vote-period-token")).willReturn(authorMemberId);
        given(jwtTokenProvider.getRolesFromAccessToken("vote-period-token")).willReturn(List.of());

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
        if (authorMemberId != null) {
            evictAuthoritySnapshotCacheUseCase.evictByMemberId(authorMemberId);
        }
    }

    @ParameterizedTest
    @CsvSource({"1800, 3600", "0, 3600", "-1800, 3600"})
    @DisplayName("시작이 마감보다 이전이고 마감이 미래이면 예정·현재 시작·진행 중 투표를 등록한다")
    void 유효한_투표_기간_등록(long startOffset, long endOffset) throws Exception {
        // Given
        AddNoticeVoteRequest request = voteRequest(startOffset, endOffset);

        // When
        mockMvc.perform(post("/api/v1/notices/{noticeId}/votes", noticeId)
                .header("Authorization", "Bearer vote-period-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // Then
        NoticeVote vote = loadNoticeVotePort.findVoteByNoticeId(noticeId).orElseThrow();
        assertThat(vote.getStartsAt()).isEqualTo(request.startsAt());
        assertThat(vote.getEndsAtExclusive()).isEqualTo(request.endsAtExclusive());
        assertThat(loadFormPort.findById(vote.getVoteId())).isPresent();
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(NOW);
    }

    @ParameterizedTest
    @CsvSource({"3600, 1800", "3600, 3600", "-3600, -1800", "-3600, 0"})
    @DisplayName("기간 역전·시작과 마감 동일·지난 마감·현재 마감은 400으로 거부하고 저장하지 않는다")
    void 잘못된_투표_기간_등록_거부(long startOffset, long endOffset) throws Exception {
        // Given
        AddNoticeVoteRequest request = voteRequest(startOffset, endOffset);
        Instant updatedAt = loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt();

        // When
        mockMvc.perform(post("/api/v1/notices/{noticeId}/votes", noticeId)
                .header("Authorization", "Bearer vote-period-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("NOTICE-CONTENTS-0014"));

        // Then
        assertThat(loadNoticeVotePort.findVoteByNoticeId(noticeId)).isEmpty();
        assertThat(entityManager.createQuery("SELECT COUNT(f) FROM Form f", Long.class).getSingleResult()).isZero();
        assertThat(loadNoticePort.findNoticeById(noticeId).orElseThrow().getUpdatedAt()).isEqualTo(updatedAt);
    }

    private AddNoticeVoteRequest voteRequest(long startOffset, long endOffset) {
        return new AddNoticeVoteRequest("투표", true, false,
            NOW.plusSeconds(startOffset), NOW.plusSeconds(endOffset), List.of("선택지 1", "선택지 2"));
    }
}
