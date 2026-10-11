package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.enums.PointType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerQueryService")
class ChallengerQueryServiceTest {

    @Mock
    LoadChallengerPort loadChallengerPort;

    @Mock
    GetChallengerPointUseCase getChallengerPointUseCase;

    @InjectMocks
    ChallengerQueryService sut;

    @Test
    @DisplayName("memberId 기준 챌린저 이력 존재 여부를 확인한다")
    void memberId_기준_챌린저_이력_존재_여부를_확인한다() {
        given(loadChallengerPort.existsByMemberId(1L)).willReturn(true);

        boolean result = sut.hasChallengerHistory(1L);

        assertThat(result).isTrue();
        then(loadChallengerPort).should().existsByMemberId(1L);
    }

    @Test
    @DisplayName("memberId가 없으면 챌린저 이력이 없다고 판단한다")
    void memberId가_없으면_챌린저_이력이_없다고_판단한다() {
        boolean result = sut.hasChallengerHistory(null);

        assertThat(result).isFalse();
        then(loadChallengerPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("상세 조회는 상벌점 이력을 최신순 전용 조회로 가져온다")
    void 상세_조회는_상벌점_이력을_최신순_전용_조회로_가져온다() {
        Challenger challenger = org.mockito.Mockito.mock(Challenger.class);
        ChallengerPointInfo point = ChallengerPointInfo.builder()
            .id(10L)
            .challengerId(1L)
            .pointType(PointType.BLOG_CHALLENGE)
            .point(3.0)
            .description("블로그 챌린지")
            .createdAt(Instant.parse("2026-10-10T09:00:00Z"))
            .build();
        given(loadChallengerPort.getById(1L)).willReturn(challenger);
        given(getChallengerPointUseCase.listByChallengerIdOrderByCreatedAtDesc(1L))
            .willReturn(List.of(point));

        ChallengerInfo result = sut.getByIdWithPointsLatestFirst(1L);

        assertThat(result.challengerPoints()).containsExactly(point);
        then(getChallengerPointUseCase).should().listByChallengerIdOrderByCreatedAtDesc(1L);
    }

}
