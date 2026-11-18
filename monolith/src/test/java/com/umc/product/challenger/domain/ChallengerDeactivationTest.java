package com.umc.product.challenger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerStatus;

@DisplayName("ChallengerDeactivation 도메인")
class ChallengerDeactivationTest {

    @ParameterizedTest
    @EnumSource(ChallengerDeactivationReason.class)
    @DisplayName("탈부 사유에 대응하는 상태로 탈부 기록을 생성한다")
    void 탈부_사유에_대응하는_상태로_탈부_기록을_생성한다(ChallengerDeactivationReason reason) {
        // given
        String reasonDetail = reason.isOther() ? "직접 입력한 사유" : null;

        // when
        ChallengerDeactivation deactivation = ChallengerDeactivation.create(
            1L,
            reason.getStatus(),
            reason,
            reasonDetail,
            "운영진 메모",
            2L
        );

        // then
        assertThat(deactivation.getStatus()).isEqualTo(reason.getStatus());
        assertThat(deactivation.getReason()).isEqualTo(reason);
        assertThat(deactivation.getReasonDetail()).isEqualTo(reasonDetail);
    }

    @Test
    @DisplayName("탈부 사유와 상태가 다르면 생성을 거절한다")
    void 탈부_사유와_상태가_다르면_생성을_거절한다() {
        assertThatThrownBy(() -> ChallengerDeactivation.create(
            1L,
            ChallengerStatus.EXPELLED,
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            2L
        )).isInstanceOf(ChallengerDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ChallengerErrorCode.INVALID_CHALLENGER_DEACTIVATION_REQUEST);
    }

    @Test
    @DisplayName("기타 사유는 상세 사유가 필수다")
    void 기타_사유는_상세_사유가_필수다() {
        assertThatThrownBy(() -> ChallengerDeactivation.create(
            1L,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_OTHER,
            " ",
            null,
            2L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("기타가 아닌 사유에는 상세 사유를 입력할 수 없다")
    void 기타가_아닌_사유에는_상세_사유를_입력할_수_없다() {
        assertThatThrownBy(() -> ChallengerDeactivation.create(
            1L,
            ChallengerStatus.EXPELLED,
            ChallengerDeactivationReason.INVOLUNTARY_EVENT_NO_SHOW,
            "추가 입력",
            null,
            2L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("상세 사유는 100자를 초과할 수 없다")
    void 상세_사유는_100자를_초과할_수_없다() {
        assertThatThrownBy(() -> ChallengerDeactivation.create(
            1L,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_OTHER,
            "가".repeat(101),
            null,
            2L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("메모는 100자를 초과할 수 없다")
    void 메모는_100자를_초과할_수_없다() {
        assertThatThrownBy(() -> ChallengerDeactivation.create(
            1L,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            "가".repeat(101),
            2L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("사진은 자신을 추가한 진행 기록과 연결된다")
    void 사진은_자신을_추가한_진행_기록과_연결된다() {
        // given
        ChallengerDeactivation deactivation = deactivation();
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation,
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            1,
            0,
            3L
        );

        // when
        ChallengerDeactivationImage image = ChallengerDeactivationImage.create(
            history,
            "image-id",
            0
        );

        // then
        assertThat(image.getAddedHistory()).isSameAs(history);
        assertThat(image.getAddedHistory().getActorMemberId()).isEqualTo(3L);
        assertThat(image.getAddedHistory().getDeactivation()).isSameAs(deactivation);
    }

    @Test
    @DisplayName("탈부 등록 기록은 근거 자료가 없어도 생성할 수 있다")
    void 탈부_등록_기록은_근거_자료가_없어도_생성할_수_있다() {
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation(),
            ChallengerDeactivationHistoryAction.REGISTERED,
            0,
            0,
            3L
        );

        assertThat(history.getImageCount()).isZero();
        assertThat(history.getLinkCount()).isZero();
    }

    @Test
    @DisplayName("근거 추가 기록에는 사진이나 링크가 하나 이상 필요하다")
    void 근거_추가_기록에는_사진이나_링크가_하나_이상_필요하다() {
        assertThatThrownBy(() -> ChallengerDeactivationHistory.create(
            deactivation(),
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            0,
            0,
            3L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("한 번의 진행 기록도 사진 5장과 링크 3개를 초과할 수 없다")
    void 한_번의_진행_기록도_근거_자료_상한을_초과할_수_없다() {
        assertThatThrownBy(() -> ChallengerDeactivationHistory.create(
            deactivation(),
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            6,
            0,
            3L
        )).isInstanceOf(ChallengerDomainException.class);

        assertThatThrownBy(() -> ChallengerDeactivationHistory.create(
            deactivation(),
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            0,
            4,
            3L
        )).isInstanceOf(ChallengerDomainException.class);
    }

    @Test
    @DisplayName("링크는 자신을 추가한 진행 기록과 연결된다")
    void 링크는_자신을_추가한_진행_기록과_연결된다() {
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation(),
            ChallengerDeactivationHistoryAction.EVIDENCE_ADDED,
            0,
            1,
            3L
        );

        ChallengerDeactivationLink link = ChallengerDeactivationLink.create(
            history,
            "https://example.com/evidence",
            0
        );

        assertThat(link.getAddedHistory()).isSameAs(history);
        assertThat(link.getAddedHistory().getActorMemberId()).isEqualTo(3L);
    }

    private ChallengerDeactivation deactivation() {
        return ChallengerDeactivation.create(
            1L,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            2L
        );
    }
}
