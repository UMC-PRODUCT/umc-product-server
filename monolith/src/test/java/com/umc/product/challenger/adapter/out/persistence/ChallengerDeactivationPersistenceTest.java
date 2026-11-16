package com.umc.product.challenger.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.umc.product.challenger.application.port.out.DeleteChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.dto.ChallengerDeactivationRetentionTarget;
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
@Import(ChallengerDeactivationPersistenceAdapter.class)
@DisplayName("탈부 관리 JPA 매핑")
class ChallengerDeactivationPersistenceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ChallengerDeactivationImageJpaRepository imageRepository;

    @Autowired
    private LoadChallengerDeactivationPort loadChallengerDeactivationPort;

    @Autowired
    private DeleteChallengerDeactivationPort deleteChallengerDeactivationPort;

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
        assertThat(imageRepository.existsByImageId("image-id")).isTrue();
        assertThat(imageRepository.existsByImageId("unknown-image-id")).isFalse();
    }

    @Test
    @DisplayName("같은 챌린저의 탈부 기록은 한 번만 저장한다")
    void 같은_챌린저의_탈부_기록은_한_번만_저장한다() {
        entityManager.persist(deactivation(1L));

        assertThatThrownBy(() -> entityManager.persist(deactivation(1L)))
            .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("보관 만료 대상과 근거 사진을 조회하고 외래 키 순서로 삭제한다")
    void listAndDeleteRetentionTarget() {
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
        entityManager.persist(ChallengerDeactivationImage.create(history, "image-id", 0));
        entityManager.persist(ChallengerDeactivationLink.create(history, "https://example.com", 0));
        entityManager.flush();

        List<ChallengerDeactivationRetentionTarget> targets = loadChallengerDeactivationPort
            .listRetentionTargets(Instant.now().plusSeconds(60), 0L, 100);

        assertThat(targets).containsExactly(
            new ChallengerDeactivationRetentionTarget(deactivation.getId(), List.of("image-id"))
        );

        entityManager.clear();
        deleteChallengerDeactivationPort.deleteImagesByDeactivationId(deactivation.getId());
        deleteChallengerDeactivationPort.deleteLinksByDeactivationId(deactivation.getId());
        deleteChallengerDeactivationPort.deleteHistoriesByDeactivationId(deactivation.getId());
        deleteChallengerDeactivationPort.deleteById(deactivation.getId());
        entityManager.flush();

        assertThat(entityManager.find(ChallengerDeactivation.class, deactivation.getId())).isNull();
    }

    @Test
    @DisplayName("보관 만료 시각과 ID 커서 및 조회 개수를 적용한다")
    void filterRetentionTargetsByThresholdCursorAndLimit() {
        Instant threshold = Instant.parse("2021-11-16T00:00:00Z");
        ChallengerDeactivation expired = deactivation(1L);
        ChallengerDeactivation boundary = deactivation(2L);
        ChallengerDeactivation notExpired = deactivation(3L);
        entityManager.persist(expired);
        entityManager.persist(boundary);
        entityManager.persist(notExpired);
        entityManager.flush();
        updateCreatedAt(expired.getId(), threshold.minusSeconds(1));
        updateCreatedAt(boundary.getId(), threshold);
        updateCreatedAt(notExpired.getId(), threshold.plusSeconds(1));
        entityManager.clear();

        List<ChallengerDeactivationRetentionTarget> allTargets = loadChallengerDeactivationPort
            .listRetentionTargets(threshold, 0L, 100);
        List<ChallengerDeactivationRetentionTarget> cursorTargets = loadChallengerDeactivationPort
            .listRetentionTargets(threshold, expired.getId(), 100);
        List<ChallengerDeactivationRetentionTarget> limitedTargets = loadChallengerDeactivationPort
            .listRetentionTargets(threshold, 0L, 1);

        assertThat(allTargets).extracting(ChallengerDeactivationRetentionTarget::deactivationId)
            .containsExactly(expired.getId(), boundary.getId());
        assertThat(cursorTargets).extracting(ChallengerDeactivationRetentionTarget::deactivationId)
            .containsExactly(boundary.getId());
        assertThat(limitedTargets).extracting(ChallengerDeactivationRetentionTarget::deactivationId)
            .containsExactly(expired.getId());
    }

    @Test
    @DisplayName("같은 사진을 참조하는 다른 탈부 기록이 있는지 확인한다")
    void checkImageReferenceOutsideDeactivation() {
        ChallengerDeactivation first = deactivation(1L);
        ChallengerDeactivation second = deactivation(2L);
        entityManager.persist(first);
        entityManager.persist(second);
        persistEvidenceImage(first, "shared-image");
        persistEvidenceImage(second, "shared-image");
        entityManager.flush();
        entityManager.clear();

        assertThat(loadChallengerDeactivationPort.existsImageReferenceOutsideDeactivation(
            "shared-image",
            first.getId()
        )).isTrue();
        assertThat(loadChallengerDeactivationPort.existsImageReferenceOutsideDeactivation(
            "shared-image",
            second.getId()
        )).isTrue();

        deleteChallengerDeactivationPort.deleteImagesByDeactivationId(first.getId());
        entityManager.flush();

        assertThat(loadChallengerDeactivationPort.existsImageReferenceOutsideDeactivation(
            "shared-image",
            second.getId()
        )).isFalse();
    }

    private void persistEvidenceImage(ChallengerDeactivation deactivation, String imageId) {
        ChallengerDeactivationHistory history = ChallengerDeactivationHistory.create(
            deactivation,
            ChallengerDeactivationHistoryAction.REGISTERED,
            1,
            0,
            10L
        );
        entityManager.persist(history);
        entityManager.persist(ChallengerDeactivationImage.create(history, imageId, 0));
    }

    private void updateCreatedAt(Long deactivationId, Instant createdAt) {
        entityManager.createNativeQuery("""
            update challenger_deactivation
            set created_at = :createdAt
            where id = :deactivationId
            """)
            .setParameter("createdAt", createdAt)
            .setParameter("deactivationId", deactivationId)
            .executeUpdate();
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
