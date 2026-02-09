package com.umc.product.recruitment.application.service.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberProfileInfo;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.GetInterviewAssignmentsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationSummaryUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewEvaluationViewUseCase;
import com.umc.product.recruitment.application.port.in.query.GetInterviewOptionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetLiveQuestionsUseCase;
import com.umc.product.recruitment.application.port.in.query.GetMyInterviewEvaluationUseCase;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo.ApplicantInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo.AppliedPartInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo.InterviewAssignmentSlotInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsInfo.SlotInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewAssignmentsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationSummaryQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationViewQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewEvaluationsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetInterviewOptionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo.CreatedBy;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsInfo.LiveQuestionInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetLiveQuestionsQuery;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationInfo;
import com.umc.product.recruitment.application.port.in.query.dto.GetMyInterviewEvaluationQuery;
import com.umc.product.recruitment.application.port.out.LoadApplicationPartPreferencePort;
import com.umc.product.recruitment.application.port.out.LoadEvaluationPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewAssignmentPort;
import com.umc.product.recruitment.application.port.out.LoadInterviewLiveQuestionPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.EvaluationProgressStatus;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import com.umc.product.recruitment.domain.exception.RecruitmentDomainException;
import com.umc.product.recruitment.domain.exception.RecruitmentErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecruitmentInterviewEvaluationQueryService implements GetInterviewEvaluationViewUseCase,
    GetMyInterviewEvaluationUseCase,
    GetInterviewEvaluationSummaryUseCase,
    GetLiveQuestionsUseCase,
    GetInterviewAssignmentsUseCase,
    GetInterviewOptionsUseCase {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final LoadInterviewAssignmentPort loadInterviewAssignmentPort;
    private final LoadInterviewLiveQuestionPort loadInterviewLiveQuestionPort;
    private final LoadEvaluationPort loadEvaluationPort;
    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final GetMemberUseCase getMemberUseCase;

    @Override
    public GetInterviewEvaluationViewInfo get(GetInterviewEvaluationViewQuery query) {
        // InterviewQuetsionSheet에서 사용자의 1, 2지망에 해당하는 파트의 사전 질문을 조회해와야 합니다.
        // 사용자의 1, 2지망은 ApplicationPartPreference 엔티티에서 조회할 수 있습니다.
        return null;
    }

    @Override
    public GetMyInterviewEvaluationInfo get(GetMyInterviewEvaluationQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = getValidatedAssignment(query.assignmentId(), query.recruitmentId());

        // 2. Application 가져오기
        Application application = assignment.getApplication();

        // 3. 내 면접 평가 조회
        return loadEvaluationPort.findByApplicationIdAndEvaluatorUserIdAndStage(
            application.getId(),
            query.memberId(),
            EvaluationStage.INTERVIEW
        ).map(evaluation -> new GetMyInterviewEvaluationInfo(
            new GetMyInterviewEvaluationInfo.MyInterviewEvaluationInfo(
                evaluation.getId(),
                evaluation.getScore(),
                evaluation.getComments(),
                evaluation.getUpdatedAt()
            )
        )).orElse(new GetMyInterviewEvaluationInfo(null));
    }

    @Override
    public GetInterviewEvaluationsInfo get(GetInterviewEvaluationSummaryQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = getValidatedAssignment(query.assignmentId(), query.recruitmentId());

        // 2. Application 가져오기
        Application application = assignment.getApplication();

        // 3. Application에 해당하는 Evaluation 리스트 가져오기
        List<Evaluation> evaluations = loadEvaluationPort.findByApplicationIdAndStage(
            application.getId(),
            EvaluationStage.INTERVIEW
        );

        if (evaluations.isEmpty()) {
            return new GetInterviewEvaluationsInfo(null, List.of());
        }

        // 4. 평가자 ID 목록 추출 & 일괄 조회
        Set<Long> evaluatorMemberIds = evaluations.stream()
            .map(Evaluation::getEvaluatorUserId)
            .collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> profileMap = getMemberUseCase.getProfiles(evaluatorMemberIds);

        // 5. 평균 점수 계산
        Double avgScore = evaluations.stream()
            .map(Evaluation::getScore)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        // 6. 평가 리스트 생성
        List<GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo> items = evaluations.stream()
            .map(evaluation -> {
                MemberProfileInfo evaluator = profileMap.get(evaluation.getEvaluatorUserId());
                return new GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo(
                    new GetInterviewEvaluationsInfo.Evaluator(
                        evaluator.id(),
                        evaluator.nickname(),
                        evaluator.name()
                    ),
                    evaluation.getScore(),
                    evaluation.getComments()
                );
            })
            .toList();

        return new GetInterviewEvaluationsInfo(avgScore, items);
    }

    @Override
    public GetLiveQuestionsInfo get(GetLiveQuestionsQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = getValidatedAssignment(query.assignmentId(), query.recruitmentId());

        // 2. application의 live questions 조회
        Long applicationId = assignment.getApplication().getId();
        List<InterviewLiveQuestion> questions = loadInterviewLiveQuestionPort.findByApplicationIdOrderByIdAsc(
            applicationId);

        if (questions.isEmpty()) {
            return new GetLiveQuestionsInfo(List.of());
        }

        // 3. 작성자 ID 목록 추출 & 일괄 조회
        Set<Long> authorMemberIds = questions.stream()
            .map(InterviewLiveQuestion::getAuthorMemberId)
            .collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> profileMap = getMemberUseCase.getProfiles(authorMemberIds);

        // 4. LiveQuestionInfo 리스트 생성
        List<LiveQuestionInfo> items = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            InterviewLiveQuestion question = questions.get(i);
            MemberProfileInfo author = profileMap.get(question.getAuthorMemberId());

            items.add(new LiveQuestionInfo(
                question.getId(),
                i + 1,  // orderNo
                question.getContent(),
                new CreatedBy(author.id(), author.nickname(), author.name()),
                question.getAuthorMemberId().equals(query.memberId())  // canEdit
            ));
        }

        return new GetLiveQuestionsInfo(items);
    }

    @Override
    public GetInterviewAssignmentsInfo get(GetInterviewAssignmentsQuery query) {
        Instant now = Instant.now();

        // 1. 모든 InterviewAssignment 조회 (Slot, Application fetch join)
        List<InterviewAssignment> allAssignments = loadInterviewAssignmentPort
            .findByRecruitmentIdWithSlotAndApplication(query.recruitmentId());

        // 2. 날짜 필터링 (드롭박스에서 선택된 날짜와 동일한 것만)
        List<InterviewAssignment> dateFiltered = allAssignments;
        if (query.date() != null) {
            dateFiltered = allAssignments.stream()
                .filter(a -> {
                    LocalDate slotDate = a.getSlot().getStartsAt().atZone(SEOUL_ZONE).toLocalDate();
                    return slotDate.equals(query.date());
                })
                .toList();
        }

        // 3. 파트 선호도 일괄 조회 (N+1 방지)
        Set<Long> applicationIds = dateFiltered.stream()
            .map(a -> a.getApplication().getId())
            .collect(Collectors.toSet());

        List<ApplicationPartPreference> allPreferences = applicationIds.isEmpty()
            ? List.of()
            : loadApplicationPartPreferencePort.findAllByApplicationIdsOrderByPriorityAsc(applicationIds);

        // applicationId -> List<ApplicationPartPreference> 매핑
        Map<Long, List<ApplicationPartPreference>> preferenceMap = allPreferences.stream()
            .collect(Collectors.groupingBy(p -> p.getApplication().getId()));

        // 4. 파트 필터링 (1지망 기준)
        List<InterviewAssignment> partFiltered = dateFiltered;
        if (query.part() != null && query.part() != PartOption.ALL) {
            ChallengerPart targetPart = ChallengerPart.valueOf(query.part().name());
            partFiltered = dateFiltered.stream()
                .filter(a -> {
                    List<ApplicationPartPreference> prefs = preferenceMap.get(a.getApplication().getId());
                    if (prefs == null || prefs.isEmpty()) {
                        return false;
                    }
                    // 1지망(priority == 1) 확인
                    return prefs.stream()
                        .filter(p -> p.getPriority() == 1)
                        .anyMatch(p -> p.getRecruitmentPart().getPart() == targetPart);
                })
                .toList();
        }

        // 5. 지원자 프로필 일괄 조회
        Set<Long> applicantMemberIds = partFiltered.stream()
            .map(a -> a.getApplication().getApplicantMemberId())
            .collect(Collectors.toSet());

        Map<Long, MemberProfileInfo> profileMap = applicantMemberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.getProfiles(applicantMemberIds);

        // 6. 평가 상태 확인을 위해 현재 사용자의 면접 평가 조회
        Set<Long> evaluatedApplicationIds = partFiltered.stream()
            .map(a -> a.getApplication().getId())
            .filter
                (appId -> loadEvaluationPort.findByApplicationIdAndEvaluatorUserIdAndStage
                        (
                            appId,
                            query.memberId(),
                            EvaluationStage.INTERVIEW
                        )
                    .isPresent()
                )
            .collect(Collectors.toSet());

        // 7. 응답 생성
        List<InterviewAssignmentSlotInfo> items = partFiltered.stream()
            .map(assignment -> {
                Application app = assignment.getApplication();
                InterviewSlot slot = assignment.getSlot();
                MemberProfileInfo applicant = profileMap.get(app.getApplicantMemberId());
                List<ApplicationPartPreference> prefs = preferenceMap.getOrDefault(app.getId(), List.of());

                // SlotInfo 생성
                SlotInfo slotInfo = new SlotInfo(
                    slot.getId(),
                    slot.getStartsAt().atZone(SEOUL_ZONE).toLocalDate(),
                    slot.getStartsAt().atZone(SEOUL_ZONE).toLocalTime(),
                    slot.getEndsAt().atZone(SEOUL_ZONE).toLocalTime()
                );

                // ApplicantInfo 생성
                ApplicantInfo applicantInfo = new ApplicantInfo(
                    applicant != null ? applicant.nickname() : null,
                    applicant != null ? applicant.name() : null
                );

                // AppliedPartInfo 리스트 생성
                List<AppliedPartInfo> appliedParts = prefs.stream()
                    .map(p -> new AppliedPartInfo(
                        p.getPriority(),
                        p.getRecruitmentPart().getPart().name(),
                        p.getRecruitmentPart().getPart().getDisplayName()
                    ))
                    .toList();

                // 서류 점수
                Double docScore = app.getDocScore() != null
                    ? app.getDocScore().doubleValue()
                    : null;

                // 평가 진행 상태 결정
                EvaluationProgressStatus status = determineEvaluationStatus(
                    slot.getStartsAt(), now, evaluatedApplicationIds.contains(app.getId())
                );

                return new InterviewAssignmentSlotInfo(
                    assignment.getId(),
                    slotInfo,
                    app.getId(),
                    applicantInfo,
                    appliedParts,
                    docScore,
                    status
                );
            })
            .toList();

        return new GetInterviewAssignmentsInfo(
            now,
            query.date(),
            query.part(),
            items
        );
    }

    @Override
    public GetInterviewOptionsInfo get(GetInterviewOptionsQuery query) {
        // 1. INTERVIEW_WINDOW 일정 조회
        RecruitmentSchedule interviewSchedule = loadRecruitmentSchedulePort.findByRecruitmentIdAndType(
            query.recruitmentId(),
            RecruitmentScheduleType.INTERVIEW_WINDOW
        );

        // 2. 날짜 리스트 생성 (start ~ end)
        List<LocalDate> dates = new ArrayList<>();
        if (interviewSchedule != null && interviewSchedule.getStartsAt() != null
            && interviewSchedule.getEndsAt() != null) {
            LocalDate startDate = interviewSchedule.getStartsAt().atZone(SEOUL_ZONE).toLocalDate();
            LocalDate endDate = interviewSchedule.getEndsAt().atZone(SEOUL_ZONE).toLocalDate();

            LocalDate current = startDate;
            while (!current.isAfter(endDate)) {
                dates.add(current);
                current = current.plusDays(1);
            }
        }

        // 3. ALL + OPEN 상태인 파트 목록
        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(query.recruitmentId());

        List<PartOption> parts = new ArrayList<>();
        parts.add(PartOption.ALL);  // ALL 먼저 추가

        recruitmentParts.stream()
            .filter(RecruitmentPart::isOpen)
            .sorted(Comparator.comparingInt(a -> a.getPart().getSortOrder()))
            .map(rp -> toPartOption(rp.getPart()))
            .forEach(parts::add);

        return new GetInterviewOptionsInfo(dates, parts);
    }

    private PartOption toPartOption(ChallengerPart challengerPart) {
        return PartOption.valueOf(challengerPart.name());
    }

    // InterviewAssignment 조회 및 검증
    private InterviewAssignment getValidatedAssignment(Long assignmentId, Long recruitmentId) {
        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(assignmentId)
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getId().equals(recruitmentId)) {
            throw new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_BELONGS_TO_RECRUITMENT);
        }

        return assignment;
    }

    // 평가 상태 결정
    private EvaluationProgressStatus determineEvaluationStatus(
        Instant interviewStartsAt, Instant now, boolean hasEvaluation
    ) {
        if (now.isBefore(interviewStartsAt)) {
            return EvaluationProgressStatus.WAITING;
        }
        return hasEvaluation ? EvaluationProgressStatus.DONE : EvaluationProgressStatus.IN_PROGRESS;
    }
}
