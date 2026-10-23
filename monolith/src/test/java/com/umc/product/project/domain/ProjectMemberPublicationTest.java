package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

class ProjectMemberPublicationTest {

    private static final Instant PUBLISH_AT = Instant.parse("2026-11-14T13:00:00Z");

    @Test
    void 발표_시각_정각부터_PENDING_PUBLICATION을_ACTIVE로_변경할_수_있다() {
        // given
        ProjectMatchingRound round = round(PUBLISH_AT);
        round.executeAutoDecision(null);
        ProjectMember member = member(round);
        assertThat(member.getAssignedMatchingRound()).isSameAs(round);

        // when & then
        assertThatThrownBy(() -> member.publish(PUBLISH_AT.minusSeconds(1)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_PUBLISHABLE);
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);

        member.publish(PUBLISH_AT);

        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    void 발표_시각이_없는_기존_차수의_멤버는_ACTIVE로_생성한다() {
        // when
        ProjectMember member = member(round(null));

        // then
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    private Project project() {
        return Project.createDraft(1L, 1L, 10L, 1L, 10L);
    }

    private ProjectMember member(ProjectMatchingRound round) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project(), 10L);
        ProjectApplication app = ProjectApplication.create(form, 100L, 20L, round);
        return ProjectMember.createFromApplication(app, ChallengerPart.WEB, null);
    }

    private ProjectMatchingRound round(Instant publishAt) {
        return ProjectMatchingRound.create("개발 1차", null, MatchingType.PLAN_DEVELOPER, MatchingPhase.FIRST,
            1L, PUBLISH_AT.minusSeconds(14400), PUBLISH_AT.minusSeconds(7200), PUBLISH_AT.minusSeconds(3600), publishAt);
    }
}
