package com.umc.product.test.domain.startingline;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;

@DisplayName("출발선 다음 단계")
class StartingLineStepTest {

    private static final ProjectQuota QUOTA = new ProjectQuota(1, ChallengerPart.WEB_PRODUCT_ENGINEER, 4);

    @Test
    @DisplayName("프로젝트가 없으면 초안부터, 초안은 정보 → 지원 폼 → 제출 순서로 남은 단계부터 이어 간다")
    void draftResumesFromMissingStep() {
        OwnedProject draft = OwnedProject.newDraft(1L, 10L);

        assertThat(StartingLineStep.next(null, QUOTA)).contains(StartingLineStep.CREATE_DRAFT);
        assertThat(StartingLineStep.next(draft, QUOTA)).contains(StartingLineStep.UPDATE_INFO);
        assertThat(StartingLineStep.next(draft.withName(), QUOTA)).contains(StartingLineStep.UPSERT_FORM);
        assertThat(StartingLineStep.next(draft.withName().withForm(), null)).contains(StartingLineStep.SUBMIT);
    }

    @Test
    @DisplayName("QA 프로젝트는 승인 대기에서 멈추고, 시딩 프로젝트는 TO가 다를 때만 TO를 넣고 공개한다")
    void pendingReviewStopsForQaAndPublishesSeeded() {
        OwnedProject pending = OwnedProject.newDraft(1L, 10L).withName().withForm()
            .withStatus(ProjectStatus.PENDING_REVIEW);

        assertThat(StartingLineStep.next(pending, null)).isEmpty();
        assertThat(StartingLineStep.next(pending, QUOTA)).contains(StartingLineStep.SET_QUOTAS);
        assertThat(StartingLineStep.next(pending.withQuotas(Map.of(
            ChallengerPart.DESIGN, 1, ChallengerPart.WEB_PRODUCT_ENGINEER, 4)), QUOTA)).contains(StartingLineStep.PUBLISH);
    }

    @Test
    @DisplayName("이미 공개된 프로젝트는 QA·시딩 모두 건드리지 않는다")
    void inProgressIsDone() {
        OwnedProject published = OwnedProject.newDraft(1L, 10L).withStatus(ProjectStatus.IN_PROGRESS);

        assertThat(StartingLineStep.next(published, null)).isEmpty();
        assertThat(StartingLineStep.next(published, QUOTA)).isEmpty();
    }
}
