package com.umc.product.notice.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.umc.product.notice.domain.enums.VoteStatus;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;

@DisplayName("공지 투표 조기 종료")
class NoticeVoteTest {

    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");

    @ParameterizedTest
    @ValueSource(longs = {-3600, 0})
    @DisplayName("진행 중이거나 시작 시각과 같으면 투표를 즉시 종료한다")
    void 시작한_투표를_조기_종료한다(long startOffset) {
        // Given
        NoticeVote vote = vote(NOW.plusSeconds(startOffset), NOW.plusSeconds(3600));

        // When
        boolean changed = vote.close(NOW);

        // Then
        assertThat(changed).isTrue();
        assertThat(vote.getOpenStatus(NOW)).isEqualTo(VoteStatus.CLOSED);
        assertThat(vote.getEndsAtExclusive()).isEqualTo(NOW);
        assertThat(vote.getStartsAt()).isEqualTo(NOW.plusSeconds(startOffset));
        assertThat(vote.getVoteId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 시각과 이후 시각에 종료를 재요청해도 최초 종료 시각을 유지한다")
    void 조기_종료를_재요청해도_변경하지_않는다() {
        // Given
        NoticeVote vote = vote(NOW.minusSeconds(3600), NOW.plusSeconds(3600));
        vote.close(NOW);

        // When
        boolean sameTimeChanged = vote.close(NOW);
        boolean laterChanged = vote.close(NOW.plusSeconds(30));

        // Then
        assertThat(sameTimeChanged).isFalse();
        assertThat(laterChanged).isFalse();
        assertThat(vote.getEndsAtExclusive()).isEqualTo(NOW);
        assertThat(vote.getOpenStatus(NOW.plusSeconds(30))).isEqualTo(VoteStatus.CLOSED);
    }

    @ParameterizedTest
    @ValueSource(longs = {-3600, 0})
    @DisplayName("마감 시각에 도달했거나 지난 투표의 종료 요청은 기존 마감 시각을 유지한다")
    void 자연_종료된_투표는_변경하지_않는다(long endOffset) {
        // Given
        Instant endsAt = NOW.plusSeconds(endOffset);
        NoticeVote vote = vote(NOW.minusSeconds(7200), endsAt);

        // When
        boolean changed = vote.close(NOW);

        // Then
        assertThat(changed).isFalse();
        assertThat(vote.getEndsAtExclusive()).isEqualTo(endsAt);
        assertThat(vote.getOpenStatus(NOW)).isEqualTo(VoteStatus.CLOSED);
    }

    @Test
    @DisplayName("시작 전 투표의 조기 종료는 거부하고 기간을 유지한다")
    void 시작_전_투표는_종료할_수_없다() {
        // Given
        Instant startsAt = NOW.plusSeconds(1);
        Instant endsAt = NOW.plusSeconds(3600);
        NoticeVote vote = vote(startsAt, endsAt);

        // When & Then
        assertThatThrownBy(() -> vote.close(NOW))
            .isInstanceOf(NoticeDomainException.class)
            .extracting("baseCode")
            .isEqualTo(NoticeErrorCode.VOTE_NOT_STARTED);
        assertThat(vote.getStartsAt()).isEqualTo(startsAt);
        assertThat(vote.getEndsAtExclusive()).isEqualTo(endsAt);
        assertThat(vote.getOpenStatus(NOW)).isEqualTo(VoteStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("나노초가 있는 종료 시각도 DB 정밀도에서 미래로 반올림되지 않는다")
    void 종료_시각은_마이크로초_정밀도로_절삭한다() {
        // Given
        Instant now = Instant.parse("2030-01-01T00:00:00.123456789Z");
        NoticeVote vote = vote(NOW.minusSeconds(3600), NOW.plusSeconds(3600));

        // When
        boolean changed = vote.close(now);

        // Then
        assertThat(changed).isTrue();
        assertThat(vote.getEndsAtExclusive()).isEqualTo(Instant.parse("2030-01-01T00:00:00.123456Z"));
        assertThat(vote.getOpenStatus(now)).isEqualTo(VoteStatus.CLOSED);
    }

    private NoticeVote vote(Instant startsAt, Instant endsAt) {
        Notice notice = Notice.create("공지", "내용", 1L, false, false);
        return NoticeVote.create(1L, notice, startsAt, endsAt);
    }
}
