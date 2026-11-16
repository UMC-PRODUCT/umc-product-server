package com.umc.product.challenger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerDeactivationReasonListInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerDeactivationQueryService")
class ChallengerDeactivationQueryServiceTest {

    @Mock
    LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @InjectMocks
    ChallengerDeactivationQueryService sut;

    @Test
    @DisplayName("탈부 근거에 연결된 이미지인지 확인한다")
    void checkImageInUse() {
        given(loadChallengerDeactivationPort.existsByImageId("image-id")).willReturn(true);

        assertThat(sut.isImageInUse("image-id")).isTrue();
    }

    @Test
    @DisplayName("이미지 ID가 없으면 저장소를 조회하지 않는다")
    void doNotQueryNullImageId() {
        assertThat(sut.isImageInUse(null)).isFalse();

        then(loadChallengerDeactivationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("자진 탈부와 타의 탈부 사유를 화면 표시 순서대로 조회한다")
    void listReasons() {
        ChallengerDeactivationReasonListInfo result = sut.listReasons();

        assertThat(result.voluntaryReasons())
            .extracting(info -> info.reason())
            .containsExactly(
                ChallengerDeactivationReason.VOLUNTARY_ACADEMIC_AND_SCHEDULE,
                ChallengerDeactivationReason.VOLUNTARY_CAREER_PREPARATION,
                ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
                ChallengerDeactivationReason.VOLUNTARY_LEARNING_DIFFICULTY_AND_WORKLOAD,
                ChallengerDeactivationReason.VOLUNTARY_ACTIVITY_EXPECTATION_MISMATCH,
                ChallengerDeactivationReason.VOLUNTARY_COMMUNICATION_AND_RELATIONSHIP,
                ChallengerDeactivationReason.VOLUNTARY_OTHER
            );
        assertThat(result.involuntaryReasons())
            .extracting(info -> info.reason())
            .containsExactly(
                ChallengerDeactivationReason.INVOLUNTARY_POINT_THRESHOLD,
                ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW,
                ChallengerDeactivationReason.INVOLUNTARY_LOW_PROJECT_PARTICIPATION,
                ChallengerDeactivationReason.INVOLUNTARY_OTHER
            );
        assertThat(result.voluntaryReasons().getLast().reasonDetailRequired()).isTrue();
        assertThat(result.involuntaryReasons().getLast().reasonDetailRequired()).isTrue();
        assertThat(result.voluntaryReasons().subList(0, 6))
            .allMatch(info -> !info.reasonDetailRequired());
        assertThat(result.involuntaryReasons().subList(0, 3))
            .allMatch(info -> !info.reasonDetailRequired());
    }
}
