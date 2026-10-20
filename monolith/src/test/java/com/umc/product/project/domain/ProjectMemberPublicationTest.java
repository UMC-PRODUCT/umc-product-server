package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;

class ProjectMemberPublicationTest {

    private static final Instant PUBLISH_AT = Instant.parse("2026-11-14T13:00:00Z");

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 1})
    void 발표_시각_정각부터_대기_팀원을_활성화할_수_있다(long secondsAfterPublishAt) {
        // given
        ProjectMatchingRound round = round(PUBLISH_AT);
        round.executeAutoDecision(null);
        ProjectMember member = member(round);
        Instant now = PUBLISH_AT.plusSeconds(secondsAfterPublishAt);
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        assertThat(member.getAssignedMatchingRound()).isSameAs(round);

        // when & then
        if (secondsAfterPublishAt < 0) {
            assertThatThrownBy(() -> member.publish(now)).isInstanceOf(ProjectDomainException.class);
            assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        } else {
            member.publish(now);
            assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        }
    }

    @Test
    void 자동_선발_미완료와_상태변경_API를_통한_우회_공개를_거부한다() {
        // given
        ProjectMatchingRound round = round(PUBLISH_AT);
        ProjectMember member = member(round);

        // when & then
        assertThatThrownBy(() -> member.publish(PUBLISH_AT.plusSeconds(1))).isInstanceOf(ProjectDomainException.class);
        assertThatThrownBy(() -> member.changeStatus(ProjectMemberStatus.ACTIVE, 10L, "활성화"))
            .isInstanceOf(ProjectDomainException.class);
        ProjectMember direct = ProjectMember.create(project(), 20L, ChallengerPart.WEB, 10L);
        assertThatThrownBy(() -> direct.changeStatus(ProjectMemberStatus.PENDING_PUBLICATION, 10L, "대기"))
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    void 기존_차수와_직접_배정은_활성_상태를_유지한다() {
        assertThat(member(round(null)).getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(ProjectMember.create(project(), 20L, ChallengerPart.WEB, 10L).getStatus())
            .isEqualTo(ProjectMemberStatus.ACTIVE);
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
