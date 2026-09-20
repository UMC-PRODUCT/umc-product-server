package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import com.umc.product.authorization.application.port.in.command.EvictAuthoritySnapshotCacheUseCase;
import com.umc.product.challenger.application.port.in.command.dto.AddChallengerTrackCommand;
import com.umc.product.challenger.application.port.out.LoadChallengerPointPort;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPointPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerTrack;

@ExtendWith(MockitoExtension.class)
@DisplayName("리크루팅 트랙의 챌린저 변환")
class ChallengerTrackCommandServiceTest {

    @Mock Environment environment;
    @Mock LoadChallengerPort loadChallengerPort;
    @Mock SaveChallengerPort saveChallengerPort;
    @Mock LoadChallengerPointPort loadChallengerPointPort;
    @Mock SaveChallengerPointPort saveChallengerPointPort;
    @Mock EvictAuthoritySnapshotCacheUseCase evictAuthoritySnapshotCacheUseCase;
    @InjectMocks ChallengerCommandService sut;

    @Test
    @DisplayName("리크루팅 합격 트랙은 동일 이름의 단일 파트로 변환한다")
    void 합격_트랙을_단일_파트로_변환한다() {
        given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L)).willReturn(Optional.empty());

        sut.addTrack(AddChallengerTrackCommand.of(1L, 9L, ChallengerTrack.WEB_PRODUCT_ENGINEER));

        ArgumentCaptor<Challenger> captor = ArgumentCaptor.forClass(Challenger.class);
        then(saveChallengerPort).should().save(captor.capture());
        assertThat(captor.getValue().getPart()).isEqualTo(ChallengerPart.WEB_PRODUCT_ENGINEER);
        assertThat(captor.getValue().isInfra()).isFalse();
        then(evictAuthoritySnapshotCacheUseCase).should().evictByMemberId(1L);
    }

    @Test
    @DisplayName("동일 기수 챌린저가 이미 있으면 합격 트랙으로 덮어쓰지 않는다")
    void 기존_챌린저의_파트를_덮어쓰지_않는다() {
        Challenger existing = Challenger.builder()
            .memberId(1L).gisuId(9L).part(ChallengerPart.DESIGN).build();
        given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L)).willReturn(Optional.of(existing));

        sut.addTrack(AddChallengerTrackCommand.of(1L, 9L, ChallengerTrack.WEB_PRODUCT_ENGINEER));

        assertThat(existing.getPart()).isEqualTo(ChallengerPart.DESIGN);
        then(saveChallengerPort).should(never()).save(any());
    }

    @Test
    @DisplayName("INFRA_PLUS는 챌린저의 단독 파트로 변환할 수 없다")
    void INFRA_PLUS는_단독_파트로_변환할_수_없다() {
        given(loadChallengerPort.findByMemberIdAndGisuId(1L, 9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.addTrack(
            AddChallengerTrackCommand.of(1L, 9L, ChallengerTrack.INFRA_PLUS)))
            .isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_LEARNING_TYPE);
        then(saveChallengerPort).should(never()).save(any());
    }
}
