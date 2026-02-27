package com.umc.product.recruitment.application.service.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
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
import com.umc.product.recruitment.application.port.out.LoadInterviewQuestionSheetPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPartPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentPort;
import com.umc.product.recruitment.application.port.out.LoadRecruitmentSchedulePort;
import com.umc.product.recruitment.domain.Application;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.Evaluation;
import com.umc.product.recruitment.domain.InterviewAssignment;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import com.umc.product.recruitment.domain.InterviewSlot;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.EvaluationProgressStatus;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.PartKey;
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
import java.util.Optional;
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
    private final LoadInterviewQuestionSheetPort loadInterviewQuestionSheetPort;
    private final LoadEvaluationPort loadEvaluationPort;
    private final LoadRecruitmentSchedulePort loadRecruitmentSchedulePort;
    private final LoadRecruitmentPartPort loadRecruitmentPartPort;
    private final LoadApplicationPartPreferencePort loadApplicationPartPreferencePort;
    private final GetMemberUseCase getMemberUseCase;
    private final LoadRecruitmentPort loadRecruitmentPort;

    @Override
    public GetInterviewEvaluationViewInfo get(GetInterviewEvaluationViewQuery query) {
        // 1. 검증: InterviewAssignment 존재 & 해당 recruitment에 속하는지
        InterviewAssignment assignment = getValidatedAssignment(query.assignmentId(), query.recruitmentId());

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        // 2. Application & 지원자 정보 가져오기
        Application application = assignment.getApplication();
        Long applicationId = application.getId();
        Long recruitmentId = query.recruitmentId();

        // 3. ApplicationPartPreference 조회 (1지망, 2지망)
        List<ApplicationPartPreference> preferences = loadApplicationPartPreferencePort
            .findAllByApplicationIdOrderByPriorityAsc(applicationId);

        // 4. 지원자 프로필 조회
        MemberInfo applicantProfile = getMemberUseCase.getMemberInfoById(application.getApplicantMemberId());

        // 5. ApplicationInfo 생성
        GetInterviewEvaluationViewInfo.ApplicationInfo applicationInfo = buildApplicationInfo(
            applicantProfile, preferences
        );

        // 6. InterviewQuestionSheetInfo 생성 (공통 + 1지망 + 2지망 + 즉석질문)
        GetInterviewEvaluationViewInfo.InterviewQuestionSheetInfo questionsInfo = buildQuestionsInfo(
            rootId, applicationId, preferences, query.memberId()
        );

        // 7. LiveEvaluationListInfo 생성 (실시간 평가 현황)
        GetInterviewEvaluationViewInfo.LiveEvaluationListInfo liveEvaluationsInfo = buildLiveEvaluationsInfo(
            applicationId
        );

        // 8. MyInterviewEvaluationInfo 생성 (내 평가)
        GetInterviewEvaluationViewInfo.MyInterviewEvaluationInfo myEvaluationInfo = buildMyEvaluationInfo(
            applicationId, query.memberId()
        );

        return new GetInterviewEvaluationViewInfo(
            assignment.getId(),
            applicationId,
            applicationInfo,
            questionsInfo,
            liveEvaluationsInfo,
            myEvaluationInfo
        );
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
        InterviewAssignment assignment = getValidatedAssignment(query.assignmentId(), query.recruitmentId());
        Application application = assignment.getApplication();

        List<Evaluation> evaluations = loadEvaluationPort.findByApplicationIdAndStage(
            application.getId(),
            EvaluationStage.INTERVIEW
        );

        if (evaluations.isEmpty()) {
            return new GetInterviewEvaluationsInfo(null, List.of());
        }

        Map<Long, MemberInfo> profileMap = getEvaluatorProfileMap(evaluations);
        Double avgScore = calculateAverageScore(evaluations);

        List<GetInterviewEvaluationsInfo.GetInterviewEvaluationInfo> items = evaluations.stream()
            .map(evaluation -> {
                MemberInfo evaluator = profileMap.get(evaluation.getEvaluatorUserId());
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

        Map<Long, MemberInfo> profileMap = getMemberUseCase.getProfiles(authorMemberIds);

        // 4. LiveQuestionInfo 리스트 생성
        List<LiveQuestionInfo> items = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            InterviewLiveQuestion question = questions.get(i);
            MemberInfo author = profileMap.get(question.getAuthorMemberId());

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

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        // 1. 모든 InterviewAssignment 조회 (Slot, Application fetch join)
        List<InterviewAssignment> allAssignments = loadInterviewAssignmentPort
            .findByRootIdWithSlotAndApplication(rootId);

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

        // 5. 지원자 프로필 일괄 조회 (N+1 방지)
        Set<Long> applicantMemberIds = partFiltered.stream()
            .map(a -> a.getApplication().getApplicantMemberId())
            .collect(Collectors.toSet());

        Map<Long, MemberInfo> profileMap = applicantMemberIds.isEmpty()
            ? Map.of()
            : getMemberUseCase.getProfiles(applicantMemberIds);

        // 6. 평가 상태 확인을 위해 현재 사용자의 면접 평가 조회
        Set<Long> applicationIdsToCheck = partFiltered.stream()
            .map(a -> a.getApplication().getId())
            .collect(Collectors.toSet());

        Set<Long> evaluatedApplicationIds = applicationIdsToCheck.isEmpty()
            ? Set.of()
            : loadEvaluationPort.findApplicationIdsWithEvaluations(
                applicationIdsToCheck, query.memberId(), EvaluationStage.INTERVIEW
            );

        // 7. 응답 생성
        List<InterviewAssignmentSlotInfo> items = partFiltered.stream()
            .map(assignment -> {
                Application app = assignment.getApplication();
                InterviewSlot slot = assignment.getSlot();
                MemberInfo applicant = profileMap.get(app.getApplicantMemberId());
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
                List<Evaluation> documentEvaluations =
                    loadEvaluationPort.findByApplicationIdAndStage(
                        app.getId(),
                        EvaluationStage.DOCUMENT
                    );

                Double docScore = documentEvaluations.isEmpty()
                    ? null
                    : documentEvaluations.stream()
                        .map(Evaluation::getScore)
                        .filter(Objects::nonNull)
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0.0);

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

        Recruitment recruitment = loadRecruitmentPort.findById(query.recruitmentId())
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        // 1. INTERVIEW_WINDOW 일정 조회
        RecruitmentSchedule interviewSchedule = loadRecruitmentSchedulePort.findByRecruitmentIdAndType(
            rootId,
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
        List<RecruitmentPart> recruitmentParts = loadRecruitmentPartPort.findByRecruitmentId(rootId);

        List<PartOption> parts = new ArrayList<>();
        parts.add(PartOption.ALL);  // ALL 먼저 추가

        recruitmentParts.stream()
            .filter(RecruitmentPart::isOpen)
            .sorted(Comparator.comparingInt(a -> a.getPart().getSortOrder()))
            .map(rp -> toPartOption(rp.getPart()))
            .distinct() // 여러 공고에 중복된 파트가 있을 수 있으므로 중복 제거
            .forEach(parts::add);

        return new GetInterviewOptionsInfo(dates, parts);
    }

    private PartOption toPartOption(ChallengerPart challengerPart) {
        return PartOption.valueOf(challengerPart.name());
    }

    // InterviewAssignment 조회 및 검증
    private InterviewAssignment getValidatedAssignment(Long assignmentId, Long recruitmentId) {

        Recruitment recruitment = loadRecruitmentPort.findById(recruitmentId)
            .orElseThrow(() -> new BusinessException(Domain.RECRUITMENT, RecruitmentErrorCode.RECRUITMENT_NOT_FOUND));
        Long rootId = recruitment.getEffectiveRootId();

        InterviewAssignment assignment = loadInterviewAssignmentPort.findById(assignmentId)
            .orElseThrow(() -> new RecruitmentDomainException(RecruitmentErrorCode.INTERVIEW_ASSIGNMENT_NOT_FOUND));

        if (!assignment.getRecruitment().getEffectiveRootId().equals(rootId)) {
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

    private GetInterviewEvaluationViewInfo.ApplicationInfo buildApplicationInfo(
        MemberInfo applicantProfile,
        List<ApplicationPartPreference> preferences
    ) {
        GetInterviewEvaluationViewInfo.Applicant applicant = new GetInterviewEvaluationViewInfo.Applicant(
            applicantProfile.nickname(),
            applicantProfile.name()
        );

        List<GetInterviewEvaluationViewInfo.AppliedPart> appliedParts = preferences.stream()
            .map(pref -> {
                ChallengerPart part = pref.getRecruitmentPart().getPart();
                return new GetInterviewEvaluationViewInfo.AppliedPart(
                    pref.getPriority(),
                    part.name(),
                    part.getDisplayName()
                );
            })
            .toList();

        return new GetInterviewEvaluationViewInfo.ApplicationInfo(applicant, appliedParts);
    }

    private GetInterviewEvaluationViewInfo.InterviewQuestionSheetInfo buildQuestionsInfo(
        Long rootId,
        Long applicationId,
        List<ApplicationPartPreference> preferences,
        Long memberId
    ) {
        // 공통 질문
        List<GetInterviewEvaluationViewInfo.InterviewQuestionInfo> common =
            getQuestionsByPartKey(rootId, PartKey.COMMON);

        // 1지망 파트 질문
        List<GetInterviewEvaluationViewInfo.InterviewQuestionInfo> firstChoice =
            getQuestionsForPriority(rootId, preferences, 1);

        // 2지망 파트 질문
        List<GetInterviewEvaluationViewInfo.InterviewQuestionInfo> secondChoice =
            getQuestionsForPriority(rootId, preferences, 2);

        // 즉석 질문 (live questions)
        List<GetInterviewEvaluationViewInfo.LiveQuestionInfo> live =
            buildLiveQuestionInfoList(applicationId, memberId);

        return new GetInterviewEvaluationViewInfo.InterviewQuestionSheetInfo(
            common, firstChoice, secondChoice, live
        );
    }

    private List<GetInterviewEvaluationViewInfo.InterviewQuestionInfo> getQuestionsByPartKey(
        Long rootId, PartKey partKey
    ) {
        return loadInterviewQuestionSheetPort
            .findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(rootId, partKey)
            .stream()
            .map(q -> new GetInterviewEvaluationViewInfo.InterviewQuestionInfo(
                q.getId(), q.getOrderNo(), q.getContent()
            ))
            .toList();
    }

    // 지망별 질문 조회 로직
    private List<GetInterviewEvaluationViewInfo.InterviewQuestionInfo> getQuestionsForPriority(
        Long rootId, List<ApplicationPartPreference> preferences, int priority
    ) {
        return findPreferenceByPriority(preferences, priority)
            .map(pref -> {
                PartKey partKey = toPartKey(pref.getRecruitmentPart().getPart());
                return getQuestionsByPartKey(rootId, partKey);
            })
            .orElse(List.of());
    }

    // 지망 순위로 preference 찾기
    private Optional<ApplicationPartPreference> findPreferenceByPriority(
        List<ApplicationPartPreference> preferences, int priority
    ) {
        return preferences.stream()
            .filter(p -> p.getPriority() == priority)
            .findFirst();
    }

    private List<GetInterviewEvaluationViewInfo.LiveQuestionInfo> buildLiveQuestionInfoList(
        Long applicationId, Long memberId
    ) {
        List<InterviewLiveQuestion> liveQuestions = loadInterviewLiveQuestionPort
            .findByApplicationIdOrderByIdAsc(applicationId);

        if (liveQuestions.isEmpty()) {
            return List.of();
        }

        Set<Long> authorMemberIds = liveQuestions.stream()
            .map(InterviewLiveQuestion::getAuthorMemberId)
            .collect(Collectors.toSet());

        Map<Long, MemberInfo> authorProfileMap = getMemberUseCase.getProfiles(authorMemberIds);

        List<GetInterviewEvaluationViewInfo.LiveQuestionInfo> result = new ArrayList<>();
        for (int i = 0; i < liveQuestions.size(); i++) {
            InterviewLiveQuestion q = liveQuestions.get(i);
            MemberInfo author = authorProfileMap.get(q.getAuthorMemberId());

            result.add(new GetInterviewEvaluationViewInfo.LiveQuestionInfo(
                q.getId(),
                i + 1,
                q.getContent(),
                new GetInterviewEvaluationViewInfo.CreatedBy(
                    author.id(), author.nickname(), author.name()
                ),
                q.getAuthorMemberId().equals(memberId)
            ));
        }

        return result;
    }

    private GetInterviewEvaluationViewInfo.LiveEvaluationListInfo buildLiveEvaluationsInfo(Long applicationId) {
        List<Evaluation> evaluations = loadEvaluationPort.findByApplicationIdAndStage(
            applicationId, EvaluationStage.INTERVIEW
        );

        if (evaluations.isEmpty()) {
            return new GetInterviewEvaluationViewInfo.LiveEvaluationListInfo(null, List.of());
        }

        Map<Long, MemberInfo> evaluatorProfileMap = getEvaluatorProfileMap(evaluations);
        Double avgScore = calculateAverageScore(evaluations);

        List<GetInterviewEvaluationViewInfo.LiveEvaluationItem> items = evaluations.stream()
            .map(e -> {
                MemberInfo evaluator = evaluatorProfileMap.get(e.getEvaluatorUserId());
                return new GetInterviewEvaluationViewInfo.LiveEvaluationItem(
                    new GetInterviewEvaluationViewInfo.Evaluator(
                        evaluator.id(), evaluator.nickname(), evaluator.name()
                    ),
                    e.getScore(),
                    e.getComments()
                );
            })
            .toList();

        return new GetInterviewEvaluationViewInfo.LiveEvaluationListInfo(avgScore, items);
    }

    // 평균 점수 계산 로직
    private Double calculateAverageScore(List<Evaluation> evaluations) {
        return evaluations.stream()
            .map(Evaluation::getScore)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

    // 평가자 프로필 조회 로직
    private Map<Long, MemberInfo> getEvaluatorProfileMap(List<Evaluation> evaluations) {
        Set<Long> evaluatorIds = evaluations.stream()
            .map(Evaluation::getEvaluatorUserId)
            .collect(Collectors.toSet());

        return getMemberUseCase.getProfiles(evaluatorIds);
    }

    private GetInterviewEvaluationViewInfo.MyInterviewEvaluationInfo buildMyEvaluationInfo(
        Long applicationId, Long memberId
    ) {
        return loadEvaluationPort.findByApplicationIdAndEvaluatorUserIdAndStage(
            applicationId, memberId, EvaluationStage.INTERVIEW
        ).map(e -> new GetInterviewEvaluationViewInfo.MyInterviewEvaluationInfo(
            e.getId(), e.getScore(), e.getComments(), e.getUpdatedAt()
        )).orElse(null);
    }

    private PartKey toPartKey(ChallengerPart challengerPart) {
        return PartKey.valueOf(challengerPart.name());
    }
}
