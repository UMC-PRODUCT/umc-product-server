package com.umc.product.project.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    void 공개된_팀원의_해제를_예약해도_상태와_지원서와_선발_차수는_유지한다() {
        // given
        ProjectMatchingRound originalRound = round(PUBLISH_AT);
        originalRound.executeAutoDecision(null);
        ProjectMember member = member(originalRound);
        member.publish(PUBLISH_AT);
        ProjectApplication originalApplication = member.getApplication();
        ProjectMatchingRound adjustmentRound = adjustmentRound();

        // when
        member.scheduleRemoval(adjustmentRound, 30L, "팀 이동");

        // then
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(member.getPendingRemovalRoundId()).isEqualTo(adjustmentRound.getId());
        assertThat(member.getApplication()).isSameAs(originalApplication);
        assertThat(member.getAssignedMatchingRound()).isSameAs(originalRound);
        assertThat(member.getStatusChangeReason()).isEqualTo("팀 이동");
        assertThat(member.getStatusChangedMemberId()).isEqualTo(30L);
    }

    @Test
    void 같은_팀에_다시_배정하면_해제_예약만_취소하고_기존_배정_정보를_유지한다() {
        // given
        ProjectMatchingRound originalRound = round(PUBLISH_AT);
        originalRound.executeAutoDecision(null);
        ProjectMember member = member(originalRound);
        member.publish(PUBLISH_AT);
        ProjectApplication originalApplication = member.getApplication();
        ProjectMatchingRound adjustmentRound = adjustmentRound();
        member.scheduleRemoval(adjustmentRound, 30L, "팀 이동");

        // when
        member.cancelRemoval(adjustmentRound, 30L);

        // then
        assertThat(member.getPendingRemovalRoundId()).isNull();
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
        assertThat(member.getApplication()).isSameAs(originalApplication);
        assertThat(member.getAssignedMatchingRound()).isSameAs(originalRound);
    }

    @Test
    void 탈퇴한_팀원을_조정_기간에_재배정하면_기존_지원서_대신_랜덤_차수를_연결한다() {
        // given
        ProjectMatchingRound originalRound = round(PUBLISH_AT);
        originalRound.executeAutoDecision(null);
        ProjectMember member = member(originalRound);
        member.publish(PUBLISH_AT);
        member.withdraw("활동 종료", 30L);
        ProjectMatchingRound adjustmentRound = adjustmentRound();

        // when
        member.assignForAdjustment(ChallengerPart.WEB, adjustmentRound, 30L);

        // then
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.PENDING_PUBLICATION);
        assertThat(member.getApplication()).isNull();
        assertThat(member.getAssignedMatchingRound()).isSameAs(adjustmentRound);
    }

    @Test
    void 해제_예약된_팀원을_탈퇴_처리하면_즉시_WITHDRAWN으로_변경하고_해제_예약을_취소한다() {
        // given
        ProjectMember member = ProjectMember.create(project(), 20L, ChallengerPart.WEB, 30L);
        member.scheduleRemoval(adjustmentRound(), 30L, "팀 이동");

        // when
        member.withdraw("활동 종료", 30L);

        // then
        assertThat(member.getStatus()).isEqualTo(ProjectMemberStatus.WITHDRAWN);
        assertThat(member.getPendingRemovalRoundId()).isNull();
    }

    @ParameterizedTest
    @CsvSource({
        "ACTIVE, COMPLETED",
        "ACTIVE, WITHDRAWN",
        "ACTIVE, DISMISSED",
        "PENDING_PUBLICATION, WITHDRAWN",
        "PENDING_PUBLICATION, DISMISSED"
    })
    void 활동_중이거나_발표_대기_중인_팀원은_허용된_종료_상태로_변경한다(
        ProjectMemberStatus currentStatus, ProjectMemberStatus newStatus
    ) {
        // given
        ProjectMember member = memberWithStatus(currentStatus);

        // when
        boolean changed = member.changeStatus(newStatus, 40L, "활동 종료");

        // then
        assertThat(changed).isTrue();
        assertThat(member.getStatus()).isEqualTo(newStatus);
        assertThat(member.getStatusChangedMemberId()).isEqualTo(40L);
        assertThat(member.getStatusChangeReason()).isEqualTo("활동 종료");
        assertThat(member.getStatusUpdatedAt()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
        "ACTIVE, ACTIVE",
        "ACTIVE, PENDING_PUBLICATION",
        "PENDING_PUBLICATION, ACTIVE",
        "PENDING_PUBLICATION, PENDING_PUBLICATION",
        "PENDING_PUBLICATION, COMPLETED",
        "COMPLETED, WITHDRAWN",
        "COMPLETED, DISMISSED",
        "WITHDRAWN, COMPLETED",
        "WITHDRAWN, DISMISSED",
        "DISMISSED, COMPLETED",
        "DISMISSED, WITHDRAWN"
    })
    void 허용하지_않는_상태_변경은_거부하고_기존_상태를_유지한다(
        ProjectMemberStatus currentStatus, ProjectMemberStatus newStatus
    ) {
        // given
        ProjectMember member = memberWithStatus(currentStatus);

        // when & then
        assertThatThrownBy(() -> member.changeStatus(newStatus, 40L, "상태 변경"))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_MEMBER_STATUS_CHANGE_NOT_ALLOWED);
        assertThat(member.getStatus()).isEqualTo(currentStatus);
    }

    @ParameterizedTest
    @EnumSource(value = ProjectMemberStatus.class, names = {"COMPLETED", "WITHDRAWN", "DISMISSED"})
    void 동일한_종료_상태를_다시_요청하면_기존_사유와_처리자와_처리_시각을_유지한다(ProjectMemberStatus status) {
        // given
        ProjectMember member = memberWithStatus(status);
        Instant previousUpdatedAt = member.getStatusUpdatedAt();

        // when
        boolean changed = member.changeStatus(status, 40L, "변경된 사유");

        // then
        assertThat(changed).isFalse();
        assertThat(member.getStatus()).isEqualTo(status);
        assertThat(member.getStatusChangeReason()).isEqualTo("기존 사유");
        assertThat(member.getStatusChangedMemberId()).isEqualTo(30L);
        assertThat(member.getStatusUpdatedAt()).isEqualTo(previousUpdatedAt);
    }

    private ProjectMember memberWithStatus(ProjectMemberStatus status) {
        if (status == ProjectMemberStatus.PENDING_PUBLICATION) {
            return member(round(PUBLISH_AT));
        }
        ProjectMember member = ProjectMember.create(project(), 20L, ChallengerPart.WEB, 30L);
        if (status != ProjectMemberStatus.ACTIVE) {
            member.changeStatus(status, 30L, "기존 사유");
        }
        return member;
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

    private ProjectMatchingRound adjustmentRound() {
        ProjectMatchingRound round = ProjectMatchingRound.create("개발 랜덤", null, MatchingType.PLAN_DEVELOPER,
            MatchingPhase.RANDOM_MATCHING, 1L, PUBLISH_AT.plusSeconds(7200), PUBLISH_AT.plusSeconds(14400),
            PUBLISH_AT.plusSeconds(14400), PUBLISH_AT.plusSeconds(18000));
        ReflectionTestUtils.setField(round, "id", 2L);
        round.executeAutoDecision(null);
        return round;
    }
}
