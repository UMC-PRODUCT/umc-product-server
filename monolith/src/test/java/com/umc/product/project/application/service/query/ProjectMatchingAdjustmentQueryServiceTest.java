package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingRosterInfo;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.service.policy.ProjectManualMatchingAccessPolicy;
import com.umc.product.project.application.service.policy.ProjectMatchingAdjustmentPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectMatchingAdjustmentQueryServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    LoadProjectMemberPort loadProjectMemberPort;
    @Mock
    CheckPermissionUseCase checkPermissionUseCase;
    @Mock
    GetMemberUseCase getMemberUseCase;
    @Spy
    ProjectMatchingAdjustmentPolicy adjustmentPolicy = new ProjectMatchingAdjustmentPolicy();
    ProjectMatchingAdjustmentQueryService sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectMatchingAdjustmentQueryService(loadProjectPort, loadProjectMatchingRoundPort, loadProjectMemberPort,
            new ProjectManualMatchingAccessPolicy(checkPermissionUseCase), getMemberUseCase, adjustmentPolicy);
    }

    @Test
    void 조정_권한이_없는_프로젝트가_하나라도_있으면_명단_전체를_거부한다() {
        // given
        given(loadProjectPort.listByIds(List.of(1L, 2L))).willReturn(List.of(project(1L, 10L), project(2L, 20L)));
        given(checkPermissionUseCase.loadSubject(900L)).willReturn(chapterPresident(10L));

        // when & then
        assertThatThrownBy(() -> sut.batchGetByProjectIds(List.of(1L, 2L), 900L))
            .isInstanceOf(ProjectDomainException.class).extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
        then(loadProjectMemberPort).shouldHaveNoInteractions();
    }

    @Test
    void 조정_시작_전에는_조정_권한을_확인한_뒤_공개_명단을_반환한다() {
        // given
        Project project = project(1L, 10L);
        ProjectMatchingRound round = randomRound(MatchingType.PLAN_DEVELOPER, Instant.now().plusSeconds(3600));
        round.executeAutoDecision(null);
        ProjectMember member = ProjectMember.create(project, 100L, ChallengerPart.WEB_PRODUCT_ENGINEER, 900L);
        given(loadProjectPort.listByIds(List.of(1L))).willReturn(List.of(project));
        given(checkPermissionUseCase.loadSubject(900L)).willReturn(chapterPresident(10L));
        given(loadProjectMatchingRoundPort.listByChapterId(10L)).willReturn(List.of(publishedThirdRound(round), round));
        given(loadProjectMemberPort.listAdjustmentMembersByProjectIds(Set.of(1L), Set.of())).willReturn(List.of(member));
        given(getMemberUseCase.findAllByIds(Set.of(99L, 100L))).willReturn(Map.of(
            99L, MemberInfo.builder().id(99L).build(), 100L, MemberInfo.builder().id(100L).build()));

        // when
        Map<Long, ProjectMatchingRosterInfo> result = sut.batchGetByProjectIds(List.of(1L), 900L);

        // then
        assertThat(result.get(1L).members()).extracting("memberId").containsExactly(100L);
        then(checkPermissionUseCase).should().loadSubject(900L);
    }

    @Test
        void 디자인_최종_공개_후_개발자_조정_기간에는_개발자_조정_차수만_명단에_반영한다() {
        // given
        Project project = project(1L, 10L);
        Instant now = Instant.now();
        ProjectMatchingRound design = randomRound(MatchingType.PLAN_DESIGN, now.minusSeconds(30L * 24 * 3600));
        ReflectionTestUtils.setField(design, "id", 11L);
        design.executeAutoDecision(null);
        design.publishResult(design.getResultPublishAt());
        ProjectMatchingRound developer = randomRound(MatchingType.PLAN_DEVELOPER, now.minusSeconds(60));
        ReflectionTestUtils.setField(developer, "id", 12L);
        developer.executeAutoDecision(null);
        ProjectMember member = ProjectMember.createForMatching(
            project, 100L, ChallengerPart.WEB_PRODUCT_ENGINEER, developer, 900L);
        given(loadProjectPort.listByIds(List.of(1L))).willReturn(List.of(project));
        given(checkPermissionUseCase.loadSubject(900L)).willReturn(chapterPresident(10L));
        given(loadProjectMatchingRoundPort.listByChapterId(10L)).willReturn(List.of(
            publishedThirdRound(design), design, publishedThirdRound(developer), developer));
        given(loadProjectMemberPort.listAdjustmentMembersByProjectIds(Set.of(1L), Set.of(12L))).willReturn(List.of(member));
        given(getMemberUseCase.findAllByIds(Set.of(99L, 100L))).willReturn(Map.of(
            99L, MemberInfo.builder().id(99L).build(), 100L, MemberInfo.builder().id(100L).build()));

        // when
        Map<Long, ProjectMatchingRosterInfo> result = sut.batchGetByProjectIds(List.of(1L), 900L);

        // then
        assertThat(result.get(1L).members()).extracting("matchingRoundId").containsExactly(12L);
        then(loadProjectMemberPort).should().listAdjustmentMembersByProjectIds(Set.of(1L), Set.of(12L));
    }

    private Project project(Long projectId, Long chapterId) {
        Project project = Project.createDraft(1L, chapterId, 99L, 1L, 99L);
        ReflectionTestUtils.setField(project, "id", projectId);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.IN_PROGRESS);
        return project;
    }

    private SubjectAttributes chapterPresident(Long chapterId) {
        return SubjectAttributes.builder().memberId(900L)
            .roleAttributes(List.of(new RoleAttribute(ChallengerRoleType.CHAPTER_PRESIDENT,
                OrganizationType.CHAPTER, chapterId, null, 1L))).build();
    }

    private ProjectMatchingRound publishedThirdRound(ProjectMatchingRound random) {
        Instant start = random.getStartsAt();
        ProjectMatchingRound third = ProjectMatchingRound.create("3차", null, random.getType(), MatchingPhase.THIRD, 10L,
            start.minusSeconds(18000), start.minusSeconds(14400), start.minusSeconds(10800), start.minusSeconds(7200));
        third.executeAutoDecision(null);
        third.publishResult(third.getResultPublishAt());
        return third;
    }

    private ProjectMatchingRound randomRound(MatchingType type, Instant start) {
        return ProjectMatchingRound.create("랜덤", null, type, MatchingPhase.RANDOM_MATCHING, 10L,
            start, start.plusSeconds(3600), start.plusSeconds(3600), start.plusSeconds(7200));
    }
}
