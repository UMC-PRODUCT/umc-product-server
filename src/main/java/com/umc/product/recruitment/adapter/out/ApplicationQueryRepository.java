package com.umc.product.recruitment.adapter.out;

import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.recruitment.domain.QApplication.application;
import static com.umc.product.recruitment.domain.QApplicationPartPreference.applicationPartPreference;
import static com.umc.product.recruitment.domain.QEvaluation.evaluation;
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
import com.umc.product.recruitment.adapter.out.dto.ApplicationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.DocumentSelectionListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.EvaluationListItemProjection;
import com.umc.product.recruitment.adapter.out.dto.MyDocumentEvaluationProjection;
import com.umc.product.recruitment.application.port.in.query.dto.DocumentSelectionApplicationListInfo;
import com.umc.product.recruitment.domain.ApplicationPartPreference;
import com.umc.product.recruitment.domain.enums.ApplicationStatus;
import com.umc.product.recruitment.domain.enums.EvaluationStage;
import com.umc.product.recruitment.domain.enums.EvaluationStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
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
     * - APPLIED / DOC_PASSED만 포함 - part 필터: 1지망(priority=1) 기준
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
                member.name,
                member.nickname,
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
     * 서류 선발 요약 조회 - totalCount: (APPLIED + DOC_PASSED) (part 필터 적용) - selectedCount: DOC_PASSED (part 필터 적용) - byPart:
     * 1지망 기준 파트별 total/selected
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
        return application.status.in(ApplicationStatus.APPLIED, ApplicationStatus.DOC_PASSED);
    }

    /**
     * part 필터: 1지망(priority=0) 기준 - ALL이면 null (필터 없음)
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

}
