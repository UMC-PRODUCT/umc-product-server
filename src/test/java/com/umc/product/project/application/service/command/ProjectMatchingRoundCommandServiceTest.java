package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.application.port.in.query.dto.ChallengerRoleInfo;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.LoadProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.LoadProjectPartQuotaPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectMatchingRoundPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.application.service.policy.DesignerMatchingPolicy;
import com.umc.product.project.application.service.policy.DeveloperMatchingPolicy;
import com.umc.product.project.application.service.policy.MatchingDecisionPolicy;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectMatchingRound;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.ProjectPartQuota;
import com.umc.product.project.domain.enums.MatchingPhase;
import com.umc.product.project.domain.enums.MatchingType;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMatchingRoundCommandServiceTest {

    private static final Long ROUND_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long GISU_ID = 5L;
    private static final Long EXECUTOR_MEMBER_ID = 999L;

    private static final Instant ROUND_DECISION_DEADLINE = Instant.now().minusSeconds(60);

    @Mock
    LoadProjectMatchingRoundPort loadProjectMatchingRoundPort;
    @Mock
    SaveProjectMatchingRoundPort saveProjectMatchingRoundPort;
    @Mock
    LoadProjectApplicationPort loadProjectApplicationPort;
    @Mock
    SaveProjectApplicationPort saveProjectApplicationPort;
    @Mock
    LoadProjectPartQuotaPort loadProjectPartQuotaPort;
    @Mock
    SaveProjectMemberPort saveProjectMemberPort;
    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    ProjectMatchingRoundCommandService sut;

    @BeforeEach
    void setUp() {
        sut = new ProjectMatchingRoundCommandService(
            loadProjectMatchingRoundPort,
            saveProjectMatchingRoundPort,
            loadProjectApplicationPort,
            saveProjectApplicationPort,
            loadProjectPartQuotaPort,
            saveProjectMemberPort,
            List.<MatchingDecisionPolicy>of(new DesignerMatchingPolicy(), new DeveloperMatchingPolicy()),
            new Random(42L),
            getChallengerRoleUseCase,
            getChallengerUseCase
        );
        // 운영진 권한 통과 mock — 권한 거부 테스트는 자체적으로 다시 stubbing
        given(getChallengerRoleUseCase.findAllByMemberId(EXECUTOR_MEMBER_ID))
            .willReturn(List.of(centralCoreRole()));
    }

    @Nested
    class autoDecide {

        @Test
        void 차수가_이미_자동선발_실행됐으면_no_op() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            ReflectionTestUtils.setField(round, "autoDecisionExecutedAt", Instant.now());
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            then(loadProjectApplicationPort).should(never()).listByMatchingRoundId(any());
            then(saveProjectApplicationPort).should(never()).saveAll(any());
            then(saveProjectMemberPort).should(never()).save(any());
        }

        @Test
        void 결정_마감_전이면_PROJECT_MATCHING_ROUND_NOT_FINALIZABLE() {
            ProjectMatchingRound round = futureDeadlineRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);

            assertThatThrownBy(() -> sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_NOT_FINALIZABLE);
        }

        @Test
        void 지원자가_없으면_round만_executeAutoDecision_처리하고_종료() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of());

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            assertThat(round.getAutoDecisionExecutedAt()).isNotNull();
            assertThat(round.getAutoDecisionExecutedMemberId()).isEqualTo(EXECUTOR_MEMBER_ID);
            then(saveProjectApplicationPort).should(never()).saveAll(any());
            then(saveProjectMemberPort).should(never()).save(any());
        }

        @Test
        void 디자이너_차수_지원자_2명_PM_미결정시_random_1명_합격_및_member_추가() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication app1 = application(101L, 11L, ProjectApplicationStatus.SUBMITTED, project, round);
            ProjectApplication app2 = application(102L, 12L, ProjectApplicationStatus.SUBMITTED, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID))
                .willReturn(List.of(app1, app2));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(11L), anyLong()))
                .willReturn(challenger(11L, ChallengerPart.DESIGN));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(12L), anyLong()))
                .willReturn(challenger(12L, ChallengerPart.DESIGN));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(partQuota(project, ChallengerPart.DESIGN, 1L)));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            long approvedCount = List.of(app1, app2).stream()
                .filter(a -> a.getStatus() == ProjectApplicationStatus.APPROVED).count();
            long rejectedCount = List.of(app1, app2).stream()
                .filter(a -> a.getStatus() == ProjectApplicationStatus.REJECTED).count();
            assertThat(approvedCount).isEqualTo(1);
            assertThat(rejectedCount).isEqualTo(1);
            then(saveProjectMemberPort).should().save(any(ProjectMember.class));
            assertThat(round.getAutoDecisionExecutedAt()).isNotNull();
        }

        @Test
        void PM이_이미_충분히_APPROVED했으면_SUBMITTED는_REJECTED로_확정되고_새_member는_PM_APPROVED만큼만_추가() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication approved = application(101L, 11L, ProjectApplicationStatus.APPROVED, project, round);
            ProjectApplication submitted = application(102L, 12L, ProjectApplicationStatus.SUBMITTED, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID))
                .willReturn(List.of(approved, submitted));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(11L), anyLong()))
                .willReturn(challenger(11L, ChallengerPart.DESIGN));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(12L), anyLong()))
                .willReturn(challenger(12L, ChallengerPart.DESIGN));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(partQuota(project, ChallengerPart.DESIGN, 1L)));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            assertThat(approved.getStatus()).isEqualTo(ProjectApplicationStatus.APPROVED);
            assertThat(submitted.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
            ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
            then(saveProjectMemberPort).should().save(captor.capture());
            assertThat(captor.getValue().getMemberId()).isEqualTo(11L);
        }

        @Test
        void PM이_모두_REJECTED한_경우_REJECTED에서_random_1명_override하여_member_추가() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication rejected1 = application(101L, 11L, ProjectApplicationStatus.REJECTED, project, round);
            ProjectApplication rejected2 = application(102L, 12L, ProjectApplicationStatus.REJECTED, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID))
                .willReturn(List.of(rejected1, rejected2));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(11L), anyLong()))
                .willReturn(challenger(11L, ChallengerPart.DESIGN));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(12L), anyLong()))
                .willReturn(challenger(12L, ChallengerPart.DESIGN));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(partQuota(project, ChallengerPart.DESIGN, 1L)));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            long approvedCount = List.of(rejected1, rejected2).stream()
                .filter(a -> a.getStatus() == ProjectApplicationStatus.APPROVED).count();
            assertThat(approvedCount).isEqualTo(1);
            then(saveProjectMemberPort).should().save(any(ProjectMember.class));
        }

        @Test
        void 의무_없는_케이스_지원자_1명_DESIGN은_REJECTED_확정되고_member_추가_없음() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication app = application(101L, 11L, ProjectApplicationStatus.SUBMITTED, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of(app));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(11L), anyLong()))
                .willReturn(challenger(11L, ChallengerPart.DESIGN));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(partQuota(project, ChallengerPart.DESIGN, 1L)));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            assertThat(app.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
            then(saveProjectMemberPort).should(never()).save(any());
        }

        @Test
        void 개발자_차수_프로젝트_파트별로_정책_독립적으로_적용된다() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DEVELOPER);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication web1 = application(201L, 21L, ProjectApplicationStatus.SUBMITTED, project, round);
            ProjectApplication web2 = application(202L, 22L, ProjectApplicationStatus.SUBMITTED, project, round);
            ProjectApplication ios1 = application(203L, 23L, ProjectApplicationStatus.SUBMITTED, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID))
                .willReturn(List.of(web1, web2, ios1));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(21L), anyLong()))
                .willReturn(challenger(21L, ChallengerPart.WEB));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(22L), anyLong()))
                .willReturn(challenger(22L, ChallengerPart.WEB));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(23L), anyLong()))
                .willReturn(challenger(23L, ChallengerPart.IOS));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(
                    partQuota(project, ChallengerPart.WEB, 2L),
                    partQuota(project, ChallengerPart.IOS, 4L)
                ));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            // WEB: 지원자 2명, TO 2 (100% 이상) → ceil(2*0.5) = 1명 합격 의무
            long webApproved = List.of(web1, web2).stream()
                .filter(a -> a.getStatus() == ProjectApplicationStatus.APPROVED).count();
            assertThat(webApproved).isEqualTo(1);

            // IOS: 지원자 1명, TO 4 (25% 이하) → 의무 없음, 모두 REJECTED
            assertThat(ios1.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        }

        @Test
        void DRAFT_또는_CANCELLED_상태_application은_정책_입력에서_제외된다() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            Project project = projectWithId(PROJECT_ID);
            ProjectApplication submitted = application(101L, 11L, ProjectApplicationStatus.SUBMITTED, project, round);
            ProjectApplication draft = application(102L, 12L, ProjectApplicationStatus.DRAFT, project, round);

            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID))
                .willReturn(List.of(submitted, draft));
            given(getChallengerUseCase.getByMemberIdAndGisuId(eq(11L), anyLong()))
                .willReturn(challenger(11L, ChallengerPart.DESIGN));
            given(loadProjectPartQuotaPort.listByProjectId(PROJECT_ID))
                .willReturn(List.of(partQuota(project, ChallengerPart.DESIGN, 1L)));

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            // DRAFT 는 그대로 유지, SUBMITTED 만 처리
            assertThat(draft.getStatus()).isEqualTo(ProjectApplicationStatus.DRAFT);
            assertThat(submitted.getStatus()).isEqualTo(ProjectApplicationStatus.REJECTED);
        }

        @Test
        void 정책이_없는_MatchingType이면_PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND() {
            ProjectMatchingRoundCommandService sutWithoutPolicy = new ProjectMatchingRoundCommandService(
                loadProjectMatchingRoundPort,
                saveProjectMatchingRoundPort,
                loadProjectApplicationPort,
                saveProjectApplicationPort,
                loadProjectPartQuotaPort,
                saveProjectMemberPort,
                List.<MatchingDecisionPolicy>of(),
                new Random(42L),
                getChallengerRoleUseCase,
                getChallengerUseCase
            );
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);

            assertThatThrownBy(() -> sutWithoutPolicy.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_POLICY_NOT_FOUND);
        }

        @Test
        void 자동_선발_실행_정보가_round에_기록된다_운영진_호출() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of());

            sut.autoDecide(ROUND_ID, EXECUTOR_MEMBER_ID);

            assertThat(round.getAutoDecisionExecutedAt()).isNotNull();
            assertThat(round.getAutoDecisionExecutedMemberId()).isEqualTo(EXECUTOR_MEMBER_ID);
        }

        @Test
        void 자동_선발_실행_정보가_round에_기록된다_스케줄러_호출_null() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of());

            sut.autoDecide(ROUND_ID, null);

            assertThat(round.getAutoDecisionExecutedAt()).isNotNull();
            assertThat(round.getAutoDecisionExecutedMemberId()).isNull();
        }

        @Test
        void 스케줄러_호출은_권한_검증을_우회한다_executedByMemberId_null() {
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of());

            sut.autoDecide(ROUND_ID, null);

            then(getChallengerRoleUseCase).should(never()).findAllByMemberId(any());
        }

        @Test
        void 운영진이_아닌_사용자가_호출하면_PROJECT_MATCHING_ROUND_ACCESS_DENIED() {
            Long unauthorizedMemberId = 555L;
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(getChallengerRoleUseCase.findAllByMemberId(unauthorizedMemberId)).willReturn(List.of());

            assertThatThrownBy(() -> sut.autoDecide(ROUND_ID, unauthorizedMemberId))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }

        @Test
        void 다른_지부_지부장이_호출하면_PROJECT_MATCHING_ROUND_ACCESS_DENIED() {
            Long otherChapterPresidentId = 777L;
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            ReflectionTestUtils.setField(round, "chapterId", 1L);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(getChallengerRoleUseCase.findAllByMemberId(otherChapterPresidentId))
                .willReturn(List.of(chapterPresidentRole(99L)));

            assertThatThrownBy(() -> sut.autoDecide(ROUND_ID, otherChapterPresidentId))
                .isInstanceOf(ProjectDomainException.class)
                .extracting("baseCode")
                .isEqualTo(ProjectErrorCode.PROJECT_MATCHING_ROUND_ACCESS_DENIED);
        }

        @Test
        void 본인_지부_지부장은_권한_통과한다() {
            Long chapterPresidentId = 888L;
            ProjectMatchingRound round = expiredRound(MatchingType.PLAN_DESIGN);
            ReflectionTestUtils.setField(round, "chapterId", 1L);
            given(loadProjectMatchingRoundPort.getById(ROUND_ID)).willReturn(round);
            given(loadProjectApplicationPort.listByMatchingRoundId(ROUND_ID)).willReturn(List.of());
            given(getChallengerRoleUseCase.findAllByMemberId(chapterPresidentId))
                .willReturn(List.of(chapterPresidentRole(1L)));

            sut.autoDecide(ROUND_ID, chapterPresidentId);

            assertThat(round.getAutoDecisionExecutedAt()).isNotNull();
        }
    }

    private ProjectMatchingRound expiredRound(MatchingType type) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "테스트 매칭", null,
            type, MatchingPhase.FIRST, 1L,
            Instant.now().minusSeconds(7_200),
            Instant.now().minusSeconds(3_600),
            ROUND_DECISION_DEADLINE
        );
        ReflectionTestUtils.setField(round, "id", ROUND_ID);
        return round;
    }

    private ProjectMatchingRound futureDeadlineRound(MatchingType type) {
        ProjectMatchingRound round = ProjectMatchingRound.create(
            "테스트 매칭", null,
            type, MatchingPhase.FIRST, 1L,
            Instant.now().minusSeconds(86_400),
            Instant.now().plusSeconds(43_200),
            Instant.now().plusSeconds(86_400)
        );
        ReflectionTestUtils.setField(round, "id", ROUND_ID);
        return round;
    }

    private Project projectWithId(Long projectId) {
        Project project = Project.createDraft(GISU_ID, 2L, 999L, 7L, 999L);
        ReflectionTestUtils.setField(project, "id", projectId);
        return project;
    }

    private ProjectApplication application(
        Long id, Long applicantMemberId, ProjectApplicationStatus status,
        Project project, ProjectMatchingRound round
    ) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project, 500L);
        ProjectApplication application = ProjectApplication.create(form, 999L, applicantMemberId, round);
        ReflectionTestUtils.setField(application, "id", id);
        ReflectionTestUtils.setField(application, "status", status);
        return application;
    }

    private ChallengerInfo challenger(Long memberId, ChallengerPart part) {
        return ChallengerInfo.builder()
            .challengerId(memberId * 10)
            .memberId(memberId)
            .gisuId(GISU_ID)
            .part(part)
            .challengerPoints(new ArrayList<>())
            .totalPoints(0.0)
            .build();
    }

    private ProjectPartQuota partQuota(Project project, ChallengerPart part, Long quota) {
        return ProjectPartQuota.create(project, part, quota, 999L);
    }

    private ChallengerRoleInfo centralCoreRole() {
        return ChallengerRoleInfo.builder()
            .roleType(ChallengerRoleType.CENTRAL_PRESIDENT)
            .organizationType(OrganizationType.CENTRAL)
            .organizationId(null)
            .gisuId(GISU_ID)
            .build();
    }

    private ChallengerRoleInfo chapterPresidentRole(Long chapterId) {
        return ChallengerRoleInfo.builder()
            .roleType(ChallengerRoleType.CHAPTER_PRESIDENT)
            .organizationType(OrganizationType.CHAPTER)
            .organizationId(chapterId)
            .gisuId(GISU_ID)
            .build();
    }
}
