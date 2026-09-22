package com.umc.product.test.application.service.qa;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.command.ManageChallengerWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageMissionFeedbackUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageMissionSubmissionUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookMissionUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageWeeklyCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.DeployChallengerWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionFeedbackCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.CreateOriginalWorkbookMissionCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.mission.EditMissionSubmissionCommand;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.domain.enums.FeedbackResult;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import com.umc.product.organization.application.port.in.command.ManageStudyGroupUseCase;
import com.umc.product.organization.application.port.in.command.dto.CreateStudyGroupCommand;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!prod")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@Transactional(propagation = Propagation.MANDATORY)
@RequiredArgsConstructor
public class QaLearningSeedScenario {

    private static final List<Course> COURSES = List.of(
        new Course(10, ChallengerPart.PLAN, "cau_schoolpresident", List.of("cau_plan")),
        new Course(10, ChallengerPart.DESIGN, "cau_schoolpresident", List.of("cau_design")),
        new Course(10, ChallengerPart.WEB, "cau_schoolpresident", List.of("cau_web")),
        new Course(10, ChallengerPart.ANDROID, "cau_schoolpresident", List.of("cau_android")),
        new Course(10, ChallengerPart.IOS, "cau_schoolpresident", List.of("cau_ios")),
        new Course(10, ChallengerPart.NODEJS, "cau_schoolpresident", List.of("cau_node")),
        new Course(10, ChallengerPart.SPRINGBOOT, "cau_schoolpresident",
            List.of("cau_springboot", "cau_multigisu")),
        new Course(11, ChallengerPart.PLAN, "cau_g11_plan_leader", List.of("cau_g11_plan")),
        new Course(11, ChallengerPart.DESIGN, "cau_g11_design_leader", List.of("cau_g11_design")),
        new Course(11, ChallengerPart.WEB_PRODUCT_ENGINEER, "cau_g11_web_leader",
            List.of("cau_g11_web", "cau_g11_web_infra", "cau_multigisu")),
        new Course(11, ChallengerPart.MOBILE_PRODUCT_ENGINEER, "cau_g11_mobile_leader",
            List.of("cau_g11_mobile", "cau_g11_mobile_infra"))
    );

    private final ManageStudyGroupUseCase manageStudyGroupUseCase;
    private final GetCurriculumUseCase getCurriculumUseCase;
    private final ManageCurriculumUseCase manageCurriculumUseCase;
    private final ManageWeeklyCurriculumUseCase manageWeeklyCurriculumUseCase;
    private final ManageOriginalWorkbookUseCase manageOriginalWorkbookUseCase;
    private final ManageOriginalWorkbookMissionUseCase manageOriginalWorkbookMissionUseCase;
    private final ManageChallengerWorkbookUseCase manageChallengerWorkbookUseCase;
    private final ManageMissionSubmissionUseCase manageMissionSubmissionUseCase;
    private final ManageMissionFeedbackUseCase manageMissionFeedbackUseCase;

    public void seed(QaSeedContext context) {
        for (Course course : COURSES) {
            seedCourse(context, course);
        }
    }

    private void seedCourse(QaSeedContext context, Course course) {
        Long gisuId = context.gisuId(course.generation());
        Long mentorId = context.memberId(course.mentorAlias());
        Set<Long> memberIds = course.learnerAliases().stream().map(context::memberId).collect(Collectors.toSet());
        manageStudyGroupUseCase.create(new CreateStudyGroupCommand(
            "[QA] " + course.generation() + "기 " + course.part().getDisplayName(),
            gisuId, course.part(), Set.of(mentorId), memberIds));

        Long curriculumId = getCurriculumUseCase.findIdByGisuIdAndPart(gisuId, course.part())
            .orElseGet(() -> manageCurriculumUseCase.create(CreateCurriculumCommand.builder()
                .gisuId(gisuId).part(course.part())
                .title("[QA] " + course.generation() + "기 " + course.part().getDisplayName()).build()));
        long weekNo = getCurriculumUseCase.getCurriculumOverview(gisuId, course.part(), null).weeks().stream()
            .mapToLong(week -> week.weekNo()).max().orElse(0) + 1;
        Instant now = Instant.now();
        Long weeklyId = manageWeeklyCurriculumUseCase.create(CreateWeeklyCurriculumCommand.builder()
            .curriculumId(curriculumId).weekNo(weekNo).isExtra(true)
            .title("[QA] 미제출·제출·통과·재제출 실습")
            .startsAt(now.minus(Duration.ofHours(1))).endsAt(now.plus(Duration.ofDays(30))).build());
        Long releaseMemberId = context.memberId(course.generation() == 11 ? "central_g11_education" : "superadmin");
        for (LearningState state : LearningState.values()) {
            seedWorkbook(weeklyId, state, memberIds, mentorId, releaseMemberId);
        }
    }

    private void seedWorkbook(
        Long weeklyId, LearningState state, Set<Long> memberIds, Long mentorId, Long releaseMemberId
    ) {
        Long originalId = manageOriginalWorkbookUseCase.create(CreateOriginalWorkbookCommand.builder()
            .weeklyCurriculumId(weeklyId).title("[QA] " + state.label)
            .description("학습 진행 상태별 화면과 제출·검토 기능을 확인합니다.")
            .content("학습 내용을 정리하고 필수 메모 미션을 제출해주세요.")
            .type(OriginalWorkbookType.EXTRA).initialStatus(OriginalWorkbookStatus.READY).build());
        Long missionId = manageOriginalWorkbookMissionUseCase.create(CreateOriginalWorkbookMissionCommand.builder()
            .originalWorkbookId(originalId).title("학습 내용 정리")
            .description("배운 점과 적용 방법을 작성해주세요.")
            .missionType(MissionType.MEMO).isNecessary(true).build());
        manageOriginalWorkbookUseCase.changeStatusForRelease(List.of(ChangeOriginalWorkbookStatusCommand.builder()
            .originalWorkbookId(originalId).status(OriginalWorkbookStatus.RELEASED)
            .requestedMemberId(releaseMemberId).build()));
        if (state == LearningState.NOT_RECEIVED) {
            return;
        }
        for (Long memberId : memberIds) {
            seedSubmission(originalId, missionId, memberId, mentorId, state);
        }
    }

    private void seedSubmission(
        Long originalId, Long missionId, Long memberId, Long mentorId, LearningState state
    ) {
        Long workbookId = manageChallengerWorkbookUseCase.batchDeploy(DeployChallengerWorkbookCommand.builder()
            .originalWorkbookIds(List.of(originalId)).requestedMemberId(memberId).build())
            .getFirst().challengerWorkbookId();
        if (state == LearningState.NOT_SUBMITTED) {
            return;
        }
        Long submissionId = manageMissionSubmissionUseCase.create(CreateMissionSubmissionCommand.builder()
            .challengerWorkbookId(workbookId).originalWorkbookMissionId(missionId)
            .requesterMemberId(memberId).content("[QA] 학습 내용과 적용 사례를 정리했습니다.").build());
        if (state == LearningState.SUBMITTED) {
            return;
        }
        manageMissionFeedbackUseCase.create(CreateMissionFeedbackCommand.builder()
            .missionSubmissionId(submissionId).reviewerMemberId(mentorId)
            .result(state == LearningState.PASSED ? FeedbackResult.PASS : FeedbackResult.FAIL)
            .content(state == LearningState.PASSED ? "핵심 개념과 적용 사례를 잘 정리했습니다." : "적용 사례를 보완해주세요.")
            .build());
        if (state == LearningState.REVISED) {
            // 기존 FAIL 피드백을 남긴 채 제출 내용을 보완한 재검토 대기 사례다.
            manageMissionSubmissionUseCase.edit(EditMissionSubmissionCommand.builder()
                .missionSubmissionId(submissionId).requesterMemberId(memberId)
                .content("[QA] 피드백에 따라 적용 사례를 보완했습니다. 재검토를 부탁드립니다.").build());
        }
    }

    private record Course(int generation, ChallengerPart part, String mentorAlias, List<String> learnerAliases) {
    }

    private enum LearningState {
        NOT_RECEIVED("미배정"),
        NOT_SUBMITTED("배정 후 미제출"),
        SUBMITTED("제출·평가 대기"),
        PASSED("통과"),
        REVISED("보완 제출·재검토 필요");

        private final String label;

        LearningState(String label) {
            this.label = label;
        }
    }
}
