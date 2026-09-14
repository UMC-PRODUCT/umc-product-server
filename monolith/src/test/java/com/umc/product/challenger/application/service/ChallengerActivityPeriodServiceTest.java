package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ActivityPeriodSummary;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuInfo;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerActivityPeriodService — 활동일 합산 로직")
class ChallengerActivityPeriodServiceTest {

    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetGisuUseCase getGisuUseCase;

    @InjectMocks
    ChallengerActivityPeriodService service;

    private ChallengerInfo challenger(Long id, Long gisuId, ChallengerStatus status) {
        return ChallengerInfo.builder()
            .challengerId(id)
            .memberId(100L)
            .gisuId(gisuId)
            .part(ChallengerPart.SPRINGBOOT)
            .challengerPoints(List.of())
            .totalPoints(0.0)
            .challengerStatus(status)
            .build();
    }

    private GisuInfo gisu(Long gisuId, Long generation, Instant startAt, Instant endAt) {
        return new GisuInfo(gisuId, generation, startAt, endAt, false);
    }

    @Test
    void ACTIVE와_GRADUATED_챌린저의_기수만_합산한다() {
        Instant farPast = Instant.now().minus(400, ChronoUnit.DAYS);
        Instant past = Instant.now().minus(200, ChronoUnit.DAYS);
        Instant pastEnd = Instant.now().minus(50, ChronoUnit.DAYS);

        List<ChallengerInfo> challengers = List.of(
            challenger(1L, 10L, ChallengerStatus.GRADUATED),
            challenger(2L, 11L, ChallengerStatus.ACTIVE),
            challenger(3L, 12L, ChallengerStatus.EXPELLED),
            challenger(4L, 13L, ChallengerStatus.WITHDRAWN)
        );

        Map<Long, GisuInfo> gisus = Map.of(
            10L, gisu(10L, 6L, farPast, past),
            11L, gisu(11L, 7L, past, pastEnd),
            12L, gisu(12L, 8L, farPast, past),
            13L, gisu(13L, 9L, past, pastEnd)
        );

        ActivityPeriodSummary summary = service.calculateActivityPeriod(challengers, gisus);

        assertThat(summary.perGisu()).hasSize(2);
        assertThat(summary.perGisu())
            .extracting(ActivityPeriodSummary.PerGisu::gisuId)
            .containsExactlyInAnyOrder(10L, 11L);
        assertThat(summary.totalActivityDays()).isGreaterThan(0L);
    }

    @Test
    void 챌린저가_없으면_빈_요약을_반환한다() {
        ActivityPeriodSummary summary = service.calculateActivityPeriod(List.of(), Map.of());

        assertThat(summary.totalActivityDays()).isZero();
        assertThat(summary.perGisu()).isEmpty();
    }

    @Test
    void EXPELLED_WITHDRAWN만_있으면_0일을_반환한다() {
        Instant past = Instant.now().minus(100, ChronoUnit.DAYS);
        Instant pastEnd = Instant.now().minus(10, ChronoUnit.DAYS);

        List<ChallengerInfo> challengers = List.of(
            challenger(1L, 10L, ChallengerStatus.EXPELLED),
            challenger(2L, 11L, ChallengerStatus.WITHDRAWN)
        );

        Map<Long, GisuInfo> gisus = Map.of(
            10L, gisu(10L, 6L, past, pastEnd),
            11L, gisu(11L, 7L, past, pastEnd)
        );

        ActivityPeriodSummary summary = service.calculateActivityPeriod(challengers, gisus);

        assertThat(summary.totalActivityDays()).isZero();
        assertThat(summary.perGisu()).isEmpty();
    }

    @Test
    void 진행중인_기수는_now까지의_일수만_포함한다() {
        Instant startOfActive = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant futureEnd = Instant.now().plus(120, ChronoUnit.DAYS);

        List<ChallengerInfo> challengers = List.of(
            challenger(1L, 10L, ChallengerStatus.ACTIVE)
        );

        Map<Long, GisuInfo> gisus = Map.of(
            10L, gisu(10L, 8L, startOfActive, futureEnd)
        );

        ActivityPeriodSummary summary = service.calculateActivityPeriod(challengers, gisus);

        // 가까운 시각(현재)이라 정확한 일수보다는 범위를 검증
        assertThat(summary.totalActivityDays()).isBetween(29L, 31L);
    }

    @Test
    void 종료된_기수는_전체_기간을_합산한다() {
        Instant start = Instant.now().minus(200, ChronoUnit.DAYS);
        Instant end = Instant.now().minus(20, ChronoUnit.DAYS);

        List<ChallengerInfo> challengers = List.of(
            challenger(1L, 10L, ChallengerStatus.GRADUATED)
        );

        Map<Long, GisuInfo> gisus = Map.of(
            10L, gisu(10L, 7L, start, end)
        );

        ActivityPeriodSummary summary = service.calculateActivityPeriod(challengers, gisus);

        assertThat(summary.totalActivityDays()).isEqualTo(ChronoUnit.DAYS.between(start, end));
    }

    @Test
    void getActivityPeriodByMemberId는_내부에서_챌린저와_기수를_조회한다() {
        Instant start = Instant.now().minus(100, ChronoUnit.DAYS);
        Instant end = Instant.now().minus(10, ChronoUnit.DAYS);

        given(getChallengerUseCase.getAllByMemberId(100L)).willReturn(List.of(
            challenger(1L, 10L, ChallengerStatus.GRADUATED)
        ));
        given(getGisuUseCase.getByIds(java.util.Set.of(10L)))
            .willReturn(List.of(gisu(10L, 7L, start, end)));

        ActivityPeriodSummary summary = service.getActivityPeriodByMemberId(100L);

        assertThat(summary.totalActivityDays()).isEqualTo(ChronoUnit.DAYS.between(start, end));
    }

    @Test
    void getActivityPeriodByMemberId는_챌린저_0건이면_빈_요약을_반환한다() {
        given(getChallengerUseCase.getAllByMemberId(100L)).willReturn(List.of());

        ActivityPeriodSummary summary = service.getActivityPeriodByMemberId(100L);

        assertThat(summary.totalActivityDays()).isZero();
        assertThat(summary.perGisu()).isEmpty();
    }
}
