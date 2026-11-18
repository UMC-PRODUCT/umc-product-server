package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationHistoryAction;
import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.support.PersistenceAdapterTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;

@PersistenceAdapterTest
@DisplayName("탈부 관리 JPA 매핑")
class ChallengerDeactivationPersistenceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("탈부 기록과 진행 기록 및 근거 자료를 함께 저장한다")
    void 탈부_기록과_진행_기록_및_근거_자료를_함께_저장한다() {
        ChallengerDeactivation deactivation = deactivation(1L);
        entityManager.persist(deactivation);
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation,
            ChallengerDeactivationHistoryAction.REGISTERED,
            1,
            1,
            10L
        );
        entityManager.persist(history);
        ChallengerDeactivationImage image = ChallengerDeactivationImage.create(
            history,
            "image-id",
            0
        );
        ChallengerDeactivationLink link = ChallengerDeactivationLink.create(
            history,
            "https://example.com/evidence",
            0
        );
        entityManager.persist(image);
        entityManager.persist(link);
        entityManager.flush();
        entityManager.clear();

        ChallengerDeactivationImage foundImage = entityManager.find(
            ChallengerDeactivationImage.class,
            image.getId()
        );
        ChallengerDeactivationLink foundLink = entityManager.find(
            ChallengerDeactivationLink.class,
            link.getId()
        );

        assertThat(foundImage.getAddedHistory().getDeactivation().getId())
            .isEqualTo(deactivation.getId());
        assertThat(foundLink.getAddedHistory().getActorMemberId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("같은 챌린저의 탈부 기록은 한 번만 저장한다")
    void 같은_챌린저의_탈부_기록은_한_번만_저장한다() {
        entityManager.persist(deactivation(1L));

        assertThatThrownBy(() -> entityManager.persist(deactivation(1L)))
            .isInstanceOf(PersistenceException.class);
    }

    private ChallengerDeactivation deactivation(Long challengerId) {
        return ChallengerDeactivation.create(
            challengerId,
            ChallengerStatus.WITHDRAWN,
            ChallengerDeactivationReason.VOLUNTARY_PERSONAL_CIRCUMSTANCES,
            null,
            null,
            10L
        );
    }
}
