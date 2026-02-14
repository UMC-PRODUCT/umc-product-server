package com.umc.product.recruitment.adapter.out;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QChapter.chapter;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.recruitment.domain.QApplication.application;
import static com.umc.product.recruitment.domain.QApplicationPartPreference.applicationPartPreference;
import static com.umc.product.recruitment.domain.QEvaluation.evaluation;
import static com.umc.product.recruitment.domain.QInterviewAssignment.interviewAssignment;
import static com.umc.product.recruitment.domain.QInterviewSlot.interviewSlot;
import static com.umc.product.recruitment.domain.QRecruitment.recruitment;
import static com.umc.product.recruitment.domain.QRecruitmentPart.recruitmentPart;
import static com.umc.product.survey.domain.QFormResponse.formResponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.recruitment.adapter.out.dto.AdminApplicationRow;
import com.umc.product.recruitment.adapter.out.dto.ApplicationIdWithFormResponseId;
import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.DocumentSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.FinalSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAlreadyScheduledApplicantRow;
import com.umc.product.recruitment.adapter.out.dto.InterviewSchedulingAvailableApplicantRow;
import com.umc.product.recruitment.adapter.out.dto.MyDocumentEvaluationProjection;
import com.umc.product.recruitment.application.port.in.PartOption;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.application.port.in.query.dto.FinalSelectionApplicationListInfo;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.QApplicationPartPreference;
import com.umc.product.recruitment.domain.QRecruitmentPart;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.EvaluationStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ApplicationQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 서류 평가 대상 지원서 목록 조회 (페이지네이션, 검색, 파트 필터링 지원)
     */
    public Page<ApplicationListItemProjection> searchApplications(
        Long recruitmentId,
        String keyword,
        String part,
        Long evaluatorId,
        Pageable pageable
    ) {
        List<ApplicationListItemProjection> content = queryFactory
            .select(Projections.constructor(ApplicationListItemProjection.class,
                application.id,
                application.applicantMemberId,
                member.name,
                member.nickname,
                JPAExpressions
                    .selectOne()
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.evaluatorUserId.eq(evaluatorId),
                        evaluation.stage.eq(EvaluationStage.DOCUMENT),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED)
                    )
                    .exists()
            ))
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                keywordContains(keyword),
                partMatches(part)
            )
            .orderBy(application.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(application.count())
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                keywordContains(keyword),
                partMatches(part)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 해당 모집의 전체 지원자 수 (필터 조건 무관)
     */
    public long countTotalApplications(Long recruitmentId) {
        Long count = queryFactory
            .select(application.count())
            .from(application)
            .where(belongsToRecruitment(recruitmentId))
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 현재 평가자가 서류 평가 완료(SUBMITTED)한 지원서 수
     */
    public long countEvaluatedApplications(Long recruitmentId, Long evaluatorId) {
        Long count = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                JPAExpressions
                    .selectOne()
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.evaluatorUserId.eq(evaluatorId),
                        evaluation.stage.eq(EvaluationStage.DOCUMENT),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED)
                    )
                    .exists()
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * 여러 Application의 파트 선호도를 한 번에 조회 (N+1 방지)
     */
    public List<ApplicationPartPreference> findPartPreferencesByApplicationIds(
        java.util.Set<Long> applicationIds
    ) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .selectFrom(applicationPartPreference)
            .join(applicationPartPreference.recruitmentPart, recruitmentPart).fetchJoin()
            .where(applicationPartPreference.application.id.in(applicationIds))
            .orderBy(applicationPartPreference.application.id.asc(), applicationPartPreference.priority.asc())
            .fetch();
    }

    /**
     * 특정 지원서에 대한 서류 평가 목록 조회 (SUBMITTED 상태만)
     */
    public List<EvaluationListItemProjection> findDocumentEvaluationsByApplicationId(Long applicationId) {
        return queryFactory
            .select(Projections.constructor(EvaluationListItemProjection.class,
                evaluation.id,
                evaluation.evaluatorUserId,
                member.name,
                member.nickname,
                evaluation.score,
                evaluation.comments
            ))
            .from(evaluation)
            .join(member).on(member.id.eq(evaluation.evaluatorUserId))
            .where(
                evaluation.application.id.eq(applicationId),
                evaluation.stage.eq(EvaluationStage.DOCUMENT),
                evaluation.status.eq(EvaluationStatus.SUBMITTED)
            )
            .orderBy(evaluation.id.asc())
            .fetch();
    }

    /**
     * 특정 지원서에 대한 서류 평가 평균 점수 조회 (SUBMITTED 상태만)
     */
    public BigDecimal calculateAvgDocScoreByApplicationId(Long applicationId) {
        Double avg = queryFactory
            .select(evaluation.score.avg())
            .from(evaluation)
            .where(
                evaluation.application.id.eq(applicationId),
                evaluation.stage.eq(EvaluationStage.DOCUMENT),
                evaluation.status.eq(EvaluationStatus.SUBMITTED),
                evaluation.score.isNotNull()
            )
            .fetchOne();

        return avg != null ? BigDecimal.valueOf(avg).setScale(1, java.math.RoundingMode.HALF_UP) : null;
    }

    /**
     * 특정 Application이 특정 Recruitment에 속하는지 확인 (application → formResponse → form → recruitment 경로)
     */
    public boolean isApplicationBelongsToRecruitment(Long applicationId, Long recruitmentId) {
        if (applicationId == null || recruitmentId == null) {
            return false;
        }

        Long count = queryFactory
            .select(application.count())
            .from(application)
            .where(
                application.id.eq(applicationId),
                belongsToRecruitment(recruitmentId)
            )
            .fetchOne();

        return count != null && count > 0;
    }

    /**
     * 특정 지원서에 대해 해당 평가자가 작성한 서류 평가 조회 (DRAFT, SUBMITTED 모두 포함)
     */
    public Optional<MyDocumentEvaluationProjection> findMyDocumentEvaluation(
        Long applicationId,
        Long evaluatorMemberId
    ) {
        MyDocumentEvaluationProjection result = queryFactory
            .select(Projections.constructor(MyDocumentEvaluationProjection.class,
                evaluation.application.id,
                evaluation.id,
                evaluation.score,
                evaluation.comments,
                evaluation.status,
                evaluation.updatedAt
            ))
            .from(evaluation)
            .where(
                evaluation.application.id.eq(applicationId),
                evaluation.evaluatorUserId.eq(evaluatorMemberId),
                evaluation.stage.eq(EvaluationStage.DOCUMENT)
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 서류 선발 리스트 조회 (페이지네이션, 파트 필터, 정렬)
     * <p>
     * - APPLIED / DOC_PASSED / DOC_FAILED만 포함 - part 필터: 1지망(priority=1) 기준
     */
    public Page<DocumentSelectionListItemProjection> searchDocumentSelections(
        Long recruitmentId,
        String part,
        String sort,
        Pageable pageable
    ) {
        List<DocumentSelectionListItemProjection> content = queryFactory
            .select(Projections.constructor(DocumentSelectionListItemProjection.class,
                application.id,
                member.nickname,
                member.name,
                application.status
            ))
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                documentSelectionStatus(),
                firstPriorityPartMatches(part)
            )
            .orderBy(documentSelectionOrderBy(sort))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(application.count())
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                documentSelectionStatus(),
                firstPriorityPartMatches(part)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 서류 선발 요약 조회 - totalCount: (APPLIED + DOC_PASSED + DOC_FAILED) (part 필터 적용) - selectedCount: DOC_PASSED (part 필터
     * 적용) - byPart: 1지망 기준 파트별 total/selected
     */
    public DocumentSelectionApplicationListInfo.Summary getDocumentSelectionSummary(Long recruitmentId, String part) {
        // total
        Long total = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                documentSelectionStatus(),
                firstPriorityPartMatches(part)
            )
            .fetchOne();

        // selected (DOC_PASSED)
        Long selected = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.DOC_PASSED),
                firstPriorityPartMatches(part)
            )
            .fetchOne();

        return new DocumentSelectionApplicationListInfo.Summary(
            total != null ? total : 0L,
            selected != null ? selected : 0L
        );
    }

    /**
     * 여러 applicationId에 대한 서류 평가 평균 점수 배치 조회 (SUBMITTED만) - 반환: applicationId -> avgScore(소수 1자리 반올림)
     */
    public Map<Long, BigDecimal> calculateAvgDocScoreByApplicationIds(Set<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(
                evaluation.application.id,
                evaluation.score.avg()
            )
            .from(evaluation)
            .where(
                evaluation.application.id.in(applicationIds),
                evaluation.stage.eq(EvaluationStage.DOCUMENT),
                evaluation.status.eq(EvaluationStatus.SUBMITTED),
                evaluation.score.isNotNull()
            )
            .groupBy(evaluation.application.id)
            .fetch();

        return rows.stream().collect(Collectors.toMap(
            t -> t.get(evaluation.application.id),
            t -> {
                Double avg = t.get(evaluation.score.avg());
                return avg == null ? null : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
            }
        ));
    }

    public Map<Long, Double> findAvgDocumentScoresByApplicationIds(Set<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // (applicationId, avgScore)
        NumberExpression<Double> avgExpression = evaluation.score.avg();

        List<Tuple> rows = queryFactory
            .select(evaluation.application.id, avgExpression)
            .from(evaluation)
            .where(
                evaluation.application.id.in(applicationIds),
                evaluation.stage.eq(EvaluationStage.DOCUMENT),
                evaluation.status.eq(EvaluationStatus.SUBMITTED),
                evaluation.score.isNotNull()
            )
            .groupBy(evaluation.application.id)
            .fetch();

        return rows.stream()
            .collect(Collectors.toMap(
                t -> t.get(evaluation.application.id),
                t -> {
                    Double avg = t.get(avgExpression);
                    if (avg == null) {
                        return null;
                    }
                    return BigDecimal.valueOf(avg)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
                }
            ));
    }

    public List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRecruitment(
        Long recruitmentId
    ) {
        return queryFactory
            .select(Projections.constructor(ApplicationIdWithFormResponseId.class,
                application.id,
                application.formResponseId
            ))
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.DOC_PASSED)
            )
            .fetch();
    }

    public List<ApplicationIdWithFormResponseId> findDocPassedApplicationIdsWithFormResponseIdsByRecruitmentAndFirstPreferredPart(
        Long recruitmentId,
        PartOption partOption
    ) {
        ChallengerPart part = ChallengerPart.valueOf(partOption.name());

        return queryFactory
            .select(Projections.constructor(ApplicationIdWithFormResponseId.class,
                application.id,
                application.formResponseId
            ))
            .from(application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.DOC_PASSED),
                recruitmentPart.part.eq(part)
            )
            .fetch();
    }

    public List<InterviewSchedulingAvailableApplicantRow> findAvailableRows(
        Long recruitmentId,
        Set<Long> availableAppIds,
        PartOption requestedPart,
        String keyword
    ) {
        if (availableAppIds == null || availableAppIds.isEmpty()) {
            return List.of();
        }

        QApplicationPartPreference pref1 = new QApplicationPartPreference("pref1");
        QApplicationPartPreference pref2 = new QApplicationPartPreference("pref2");
        QRecruitmentPart rp1 = new QRecruitmentPart("rp1");
        QRecruitmentPart rp2 = new QRecruitmentPart("rp2");

        ChallengerPart filterPart = toFilterPart(requestedPart);

        return queryFactory
            .select(Projections.constructor(
                InterviewSchedulingAvailableApplicantRow.class,
                application.id,
                member.nickname,
                member.name,
                rp1.part,
                rp2.part
            ))
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))

            .leftJoin(pref1).on(pref1.application.eq(application), pref1.priority.eq(1))
            .leftJoin(pref1.recruitmentPart, rp1)

            .leftJoin(pref2).on(pref2.application.eq(application), pref2.priority.eq(2))
            .leftJoin(pref2.recruitmentPart, rp2)

            .where(
                application.id.in(availableAppIds),
                keywordContains(keyword),
                filterPart == null ? null : rp1.part.eq(filterPart)
            )
            .orderBy(application.createdAt.asc())
            .fetch();
    }

    public List<InterviewSchedulingAlreadyScheduledApplicantRow> findAlreadyScheduledRows(
        Long recruitmentId,
        Long slotId,
        Set<Long> alreadyScheduledAppIds,
        PartOption requestedPart,
        String keyword
    ) {
        if (alreadyScheduledAppIds == null || alreadyScheduledAppIds.isEmpty()) {
            return List.of();
        }

        QApplicationPartPreference pref1 = new QApplicationPartPreference("pref1");
        QApplicationPartPreference pref2 = new QApplicationPartPreference("pref2");
        QRecruitmentPart rp1 = new QRecruitmentPart("rp1");
        QRecruitmentPart rp2 = new QRecruitmentPart("rp2");

        ChallengerPart filterPart = toFilterPart(requestedPart);

        return queryFactory
            .select(Projections.constructor(
                InterviewSchedulingAlreadyScheduledApplicantRow.class,
                interviewAssignment.application.id,
                interviewAssignment.id,
                member.nickname,
                member.name,
                rp1.part,
                rp2.part,
                interviewSlot.startsAt,
                interviewSlot.endsAt
            ))
            .from(interviewAssignment)
            .join(interviewAssignment.slot, interviewSlot)
            .join(interviewAssignment.application, application)
            .join(member).on(member.id.eq(application.applicantMemberId))

            .leftJoin(pref1).on(pref1.application.eq(application), pref1.priority.eq(1))
            .leftJoin(pref1.recruitmentPart, rp1)

            .leftJoin(pref2).on(pref2.application.eq(application), pref2.priority.eq(2))
            .leftJoin(pref2.recruitmentPart, rp2)

            .where(
                interviewAssignment.recruitment.id.eq(recruitmentId),
                interviewAssignment.slot.id.ne(slotId), // "다른 슬롯"만
                interviewAssignment.application.id.in(alreadyScheduledAppIds),
                keywordContains(keyword),
                filterPart == null ? null : rp1.part.eq(filterPart)
            )
            .orderBy(interviewAssignment.createdAt.asc())
            .fetch();
    }

    public Map<Long, BigDecimal> calculateAvgInterviewScoreByApplicationIds(Set<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(evaluation.application.id, evaluation.score.avg())
            .from(evaluation)
            .where(
                evaluation.application.id.in(applicationIds),
                evaluation.stage.eq(EvaluationStage.INTERVIEW),
                evaluation.status.eq(EvaluationStatus.SUBMITTED),
                evaluation.score.isNotNull()
            )
            .groupBy(evaluation.application.id)
            .fetch();

        return rows.stream().collect(Collectors.toMap(
            t -> t.get(evaluation.application.id),
            t -> {
                Double avg = t.get(evaluation.score.avg());
                return avg == null ? null : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
            }
        ));
    }

    public FinalSelectionApplicationListInfo.Summary getFinalSelectionSummary(Long recruitmentId, String part) {
        Long total = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                finalSelectionStatus(),
                firstPriorityPartMatches(part)
            )
            .fetchOne();

        Long selected = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.FINAL_ACCEPTED),
                firstPriorityPartMatches(part)
            )
            .fetchOne();

        return new FinalSelectionApplicationListInfo.Summary(
            total != null ? total : 0L,
            selected != null ? selected : 0L
        );
    }


    public Page<FinalSelectionListItemProjection> searchFinalSelections(
        Long recruitmentId,
        String part,
        String sort,
        Pageable pageable
    ) {
        List<FinalSelectionListItemProjection> content = queryFactory
            .select(Projections.constructor(FinalSelectionListItemProjection.class,
                application.id,
                member.nickname,
                member.name,
                application.status,
                application.selectedPart
            ))
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                finalSelectionStatus(),
                firstPriorityPartMatches(part)
            )
            .orderBy(finalSelectionOrderBy(sort))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(application.count())
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitment(recruitmentId),
                finalSelectionStatus(),
                firstPriorityPartMatches(part)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private ChallengerPart toFilterPart(PartOption requestedPart) {
        if (requestedPart == null || requestedPart == PartOption.ALL) {
            return null;
        }
        return ChallengerPart.valueOf(requestedPart.name());
    }

    // ========================================================================
    // Private 헬퍼 메서드
    // ========================================================================

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return member.name.containsIgnoreCase(keyword)
            .or(member.nickname.containsIgnoreCase(keyword));
    }

    private BooleanExpression partMatches(String part) {
        if (!StringUtils.hasText(part) || "ALL".equalsIgnoreCase(part)) {
            return null;
        }

        ChallengerPart challengerPart;
        try {
            challengerPart = ChallengerPart.valueOf(part.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid part parameter: {}", part);
            return null;
        }

        // 지원서의 지원 파트(1지망, 2지망 등) 중 해당 파트가 있는지 확인
        return JPAExpressions
            .selectOne()
            .from(applicationPartPreference)
            .join(recruitmentPart).on(recruitmentPart.id.eq(applicationPartPreference.recruitmentPart.id))
            .where(
                applicationPartPreference.application.id.eq(application.id),
                recruitmentPart.part.eq(challengerPart)
            )
            .exists();
    }

    // ========================================================================
    // Recruitment ↔ Application 연결 로직 (application → formResponse → form → recruitment)
    // 현재: Recruitment.formId로 1:1 연결
    // 향후 1:N 변경 시 : 아래 메서드들만 수정하면 됨
    // ========================================================================

    /**
     * 특정 Recruitment에 연결된 Form ID(들)을 조회하는 서브쿼리
     * <p>
     * 현재: Recruitment.formId (1:1) 향후 1:N 변경 시: RecruitmentForm 중간 테이블이나 Form.recruitmentId 등으로 수정
     */
    private JPQLQuery<Long> formIdsForRecruitment(Long recruitmentId) {
        return JPAExpressions
            .select(recruitment.formId)
            .from(recruitment)
            .where(recruitment.id.eq(recruitmentId));
    }

    /**
     * 특정 Recruitment에 속한 Application인지 판별하는 조건 (application → formResponse → form → recruitment 경로)
     */
    private BooleanExpression belongsToRecruitment(Long recruitmentId) {
        if (recruitmentId == null) {
            return null;
        }

        // 1. recruitmentId에 해당하는 formId(들) 조회
        JPQLQuery<Long> formIds = formIdsForRecruitment(recruitmentId);

        // 2. 해당 form(들)에 대한 formResponseId(들) 조회
        JPQLQuery<Long> formResponseIds = JPAExpressions
            .select(formResponse.id)
            .from(formResponse)
            .where(formResponse.form.id.in(formIds));

        // 3. application.formResponseId가 위 서브쿼리에 포함되는지 확인
        return application.formResponseId.in(formResponseIds);
    }

    private BooleanExpression documentSelectionStatus() {
        return application.status.in(
            ApplicationStatus.APPLIED,
            ApplicationStatus.DOC_PASSED,
            ApplicationStatus.DOC_FAILED);
    }

    /**
     * part 필터: 1지망(priority=1) 기준 - ALL이면 null (필터 없음)
     */
    private BooleanExpression firstPriorityPartMatches(String part) {
        if (!StringUtils.hasText(part) || "ALL".equalsIgnoreCase(part)) {
            return null;
        }

        ChallengerPart challengerPart;
        try {
            challengerPart = ChallengerPart.valueOf(part.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid part parameter: {}", part);
            return null;
        }

        return JPAExpressions
            .selectOne()
            .from(applicationPartPreference)
            .join(recruitmentPart).on(recruitmentPart.id.eq(applicationPartPreference.recruitmentPart.id))
            .where(
                applicationPartPreference.application.id.eq(application.id),
                applicationPartPreference.priority.eq(1),
                recruitmentPart.part.eq(challengerPart)
            )
            .exists();
    }

    /**
     * 정렬 - SCORE_DESC/SCORE_ASC: application.docScore 기준 (null은 뒤로) - EVALUATED_AT_ASC: 제출된 서류평가 updatedAt의 max 기준 오름차순
     * (null은 뒤로)
     */
    private com.querydsl.core.types.OrderSpecifier<?>[] documentSelectionOrderBy(String sort) {
        String s = (sort == null) ? "SCORE_DESC" : sort;

        NumberExpression<Double> avgDocScore =
            Expressions.numberTemplate(
                Double.class,
                "({0})",
                JPAExpressions
                    .select(evaluation.score.avg())
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.stage.eq(EvaluationStage.DOCUMENT),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED),
                        evaluation.score.isNotNull()
                    )
            );

        DateTimeExpression<Instant> evaluatedAtMax =
            Expressions.dateTimeTemplate(
                Instant.class,
                "({0})",
                JPAExpressions
                    .select(evaluation.updatedAt.max())
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.stage.eq(EvaluationStage.DOCUMENT),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED)
                    )
            );

        return switch (s) {
            case "SCORE_ASC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                avgDocScore.asc().nullsLast(),
                application.id.asc()
            };
            case "EVALUATED_AT_ASC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                evaluatedAtMax.asc().nullsLast(),
                application.id.asc()
            };
            case "SCORE_DESC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                avgDocScore.desc().nullsLast(),
                application.id.asc()
            };
            default -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                avgDocScore.desc().nullsLast(),
                application.id.asc()
            };
        };
    }


    public long countByRecruitmentId(Long recruitmentId) {
        return queryFactory
            .select(application.count())
            .from(application)
            .where(belongsToRecruitment(recruitmentId))
            .fetchOne();
    }

    public long countByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, ChallengerPart part) {
        Long count = queryFactory
            .select(application.countDistinct())
            .from(application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                belongsToRecruitment(recruitmentId),
                recruitmentPart.part.eq(part)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    public List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRecruitment(
        Long recruitmentId) {
        return queryFactory
            .select(Projections.constructor(ApplicationIdWithFormResponseId.class,
                application.id,
                application.formResponseId
            ))
            .from(application)
            .where(belongsToRecruitment(recruitmentId))
            .fetch();
    }

    public List<ApplicationIdWithFormResponseId> findApplicationIdsWithFormResponseIdsByRecruitmentAndFirstPreferredPart(
        Long recruitmentId,
        PartOption partOption
    ) {
        ChallengerPart part = ChallengerPart.valueOf(partOption.name());

        return queryFactory
            .select(Projections.constructor(ApplicationIdWithFormResponseId.class,
                application.id,
                application.formResponseId
            ))
            .from(application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                belongsToRecruitment(recruitmentId),
                recruitmentPart.part.eq(part)
            )
            .fetch();
    }

    private BooleanExpression finalSelectionStatus() {
        return application.status.in(
            ApplicationStatus.DOC_PASSED,
            ApplicationStatus.FINAL_ACCEPTED,
            ApplicationStatus.FINAL_REJECTED
        );
    }

    /**
     * 정렬 - SCORE_DESC/SCORE_ASC: (서류평균 + 면접평균)/2 기준 (null은 뒤로) - EVALUATED_AT_ASC: 제출된 면접평가 updatedAt의 max 기준 오름차순
     * (null은 뒤로)
     */
    private com.querydsl.core.types.OrderSpecifier<?>[] finalSelectionOrderBy(String sort) {
        String s = (sort == null) ? "SCORE_DESC" : sort;

        // avg doc
        NumberExpression<Double> avgDocScore =
            Expressions.numberTemplate(
                Double.class,
                "({0})",
                JPAExpressions
                    .select(evaluation.score.avg())
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.stage.eq(EvaluationStage.DOCUMENT),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED),
                        evaluation.score.isNotNull()
                    )
            );

        // avg interview
        NumberExpression<Double> avgInterviewScore =
            Expressions.numberTemplate(
                Double.class,
                "({0})",
                JPAExpressions
                    .select(evaluation.score.avg())
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.stage.eq(EvaluationStage.INTERVIEW),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED),
                        evaluation.score.isNotNull()
                    )
            );

        // final = (doc + interview) / 2
        NumberExpression<Double> finalScore =
            avgDocScore.add(avgInterviewScore).divide(2.0);

        DateTimeExpression<Instant> interviewEvaluatedAtMax =
            Expressions.dateTimeTemplate(
                Instant.class,
                "({0})",
                JPAExpressions
                    .select(evaluation.updatedAt.max())
                    .from(evaluation)
                    .where(
                        evaluation.application.id.eq(application.id),
                        evaluation.stage.eq(EvaluationStage.INTERVIEW),
                        evaluation.status.eq(EvaluationStatus.SUBMITTED)
                    )
            );

        return switch (s) {
            case "SCORE_ASC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                finalScore.asc().nullsLast(),
                application.id.asc()
            };
            case "EVALUATED_AT_ASC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                interviewEvaluatedAtMax.asc().nullsLast(),
                application.id.asc()
            };
            case "SCORE_DESC" -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                finalScore.desc().nullsLast(),
                application.id.asc()
            };
            default -> new com.querydsl.core.types.OrderSpecifier<?>[]{
                finalScore.desc().nullsLast(),
                application.id.asc()
            };
        };
    }

    public Page<AdminApplicationRow> searchAdminApplications(
        Long chapterId,
        Long schoolId,
        String part,
        String keyword,
        Pageable pageable
    ) {
        List<AdminApplicationRow> content = queryFactory
            .select(Projections.constructor(
                AdminApplicationRow.class,
                application.id,
                application.applicantMemberId,
                member.nickname,
                member.name,
                member.email,              // 이메일 검색용
                school.id,
                school.name,
                application.status,
                application.selectedPart   // ChallengerPart (FINAL_ACCEPTED일 때만 존재)
            ))
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .leftJoin(school).on(school.id.eq(member.schoolId)) // member가 schoolId만 들고 있음
            .where(
                belongsToRecruitments(chapterId, schoolId), // recruitment 기준 필터
                keywordContainsForAdmin(keyword),           // name/nickname/email
                partMatches(part)                           // 1~2지망 포함 exists
            )
            .orderBy(
                application.createdAt.asc(),
                application.id.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(application.count())
            .from(application)
            .join(member).on(member.id.eq(application.applicantMemberId))
            .where(
                belongsToRecruitments(chapterId, schoolId),
                keywordContainsForAdmin(keyword),
                partMatches(part)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public long countDocPassedByRecruitmentId(Long recruitmentId) {
        Long count = queryFactory
            .select(application.count())
            .from(application)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.DOC_PASSED)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    public long countDocPassedByRecruitmentIdAndFirstPreferredPart(Long recruitmentId, ChallengerPart part) {
        Long count = queryFactory
            .select(application.countDistinct())
            .from(application)
            .join(applicationPartPreference).on(
                applicationPartPreference.application.eq(application),
                applicationPartPreference.priority.eq(1)
            )
            .join(applicationPartPreference.recruitmentPart, recruitmentPart)
            .where(
                belongsToRecruitment(recruitmentId),
                application.status.eq(ApplicationStatus.DOC_PASSED),
                recruitmentPart.part.eq(part)
            )
            .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression belongsToRecruitments(Long chapterId, Long schoolId) {
        // 둘 다 null이면 전체(필터 없음)
        if (chapterId == null && schoolId == null) {
            return null;
        }

        // 조건에 맞는 recruitment.id들
        JPQLQuery<Long> recruitmentIds = JPAExpressions
            .select(recruitment.id)
            .from(recruitment)
            .where(
                schoolId == null ? null : recruitment.schoolId.eq(schoolId),
                chapterId == null ? null : belongsToChapter(chapterId),
                recruitment.formId.isNotNull()
            );

        // 해당 recruitment들의 formId
        JPQLQuery<Long> formIds = formIdsForRecruitments(recruitmentIds);

        // 해당 form들에 대한 formResponse.id
        JPQLQuery<Long> formResponseIds = JPAExpressions
            .select(formResponse.id)
            .from(formResponse)
            .where(formResponse.form.id.in(formIds));

        return application.formResponseId.in(formResponseIds);
    }

    /**
     * chapterId 필터: recruitment.schoolId + recruitment.gisuId에 대해, ChapterSchool(school_id) - Chapter(chapter_id,
     * gisu_id)가 매칭되는지 존재 여부로 판별
     */
    private BooleanExpression belongsToChapter(Long chapterId) {
        return JPAExpressions
            .selectOne()
            .from(chapterSchool)
            .join(chapterSchool.chapter, chapter)
            .where(
                chapter.id.eq(chapterId),
                // chapterSchool.school.id 와 recruitment.schoolId 매칭
                chapterSchool.school.id.eq(recruitment.schoolId),
                // chapter.gisu.id 와 recruitment.gisuId 매칭 (기수별 지부-학교 배정 구분)
                chapter.gisu.id.eq(recruitment.gisuId)
            )
            .exists();
    }

    /**
     * "recruitmentId 집합 -> formId" 집합을 뽑는 서브쿼리 현재(1:1): recruitment.formId 향후(1:N): 이 함수만 수정
     */
    private JPQLQuery<Long> formIdsForRecruitments(JPQLQuery<Long> recruitmentIds) {
        return JPAExpressions
            .select(recruitment.formId)
            .from(recruitment)
            .where(recruitment.id.in(recruitmentIds));
    }

    private BooleanExpression keywordContainsForAdmin(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return member.name.containsIgnoreCase(keyword)
            .or(member.nickname.containsIgnoreCase(keyword))
            .or(member.email.containsIgnoreCase(keyword));
    }
}
