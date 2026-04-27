package com.umc.product.notice.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.notice.domain.QNotice.notice;
import static com.umc.product.notice.domain.QNoticeRead.noticeRead;
import static com.umc.product.notice.domain.QNoticeTarget.noticeTarget;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.query.dto.NoticeViewerInfo;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeTargetRole;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeClassification;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;

    /*
     * 분류 (전체, 지부, 학교, 파트) 별 조회
     *
     * 조회자의 소속 정보를 기반으로 해당하는 공지를 조회합니다.
     * 각 레벨에서는 해당 레벨의 공지 + 전체 대상 공지를 함께 조회합니다.
     *
     * 조회 조건:
     * - 전체 조회 (gisuId만): 기수 전체 대상 공지 (지부/학교/파트 모두 NULL)
     * - 지부 조회 (gisuId + chapterId): 해당 지부 공지 + 전체 지부 대상 공지
     * - 학교 조회 (+ schoolId): 해당 학교 공지 + 전체 학교 대상 공지
     * - 파트 조회 (+ part): 해당 파트 공지 + 전체 파트 대상 공지
     *
     * @return Notice 페이지
     */
    public Page<Notice> findByClassification(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo,
        Pageable pageable) {

        BooleanExpression condition = buildCombinedCondition(classification, viewerInfo);

        List<Notice> content = getContentQuery(condition, null, pageable);
        Long total = getTotalCountQuery(condition, null);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /*
     * 검색어 기반 조회
     * @return Notice 페이지
     */
    public Page<Notice> findByKeyword(
        String keyword,
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo,
        Pageable pageable) {

        BooleanExpression condition = buildCombinedCondition(classification, viewerInfo);

        List<Notice> notices = getContentQuery(condition, keyword, pageable);
        Long total = getTotalCountQuery(condition, keyword);

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }

    /*
     * 안 읽은 챌린저 조회
     * @return 안 읽은 챌린저 ID 리스트
     */
    public List<Long> findUnreadChallengerIdByNoticeId(Long noticeId) {
        return queryFactory
            .select(challenger.id)
            .from(challenger)
            .where(
                challenger.id.notIn(
                    JPAExpressions
                        .select(noticeRead.challengerId)
                        .from(noticeRead)
                        .where(noticeRead.notice.id.eq(noticeId))
                )
            )
            .fetch();
    }

    /*
     * 여러 공지사항의 읽은 사람 수를 한 번에 조회
     * @return noticeId → 읽음 수 Map
     */
    public Map<Long, Long> countReadsByNoticeIds(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> results = queryFactory
            .select(noticeRead.notice.id, noticeRead.count())
            .from(noticeRead)
            .where(noticeRead.notice.id.in(noticeIds))
            .groupBy(noticeRead.notice.id)
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(noticeRead.notice.id),
                tuple -> tuple.get(noticeRead.count())
            ));
    }

    // ========== PRIVATE ====================

    /**
     * 챌린저 공지와 운영진 공지 조건을 OR로 결합합니다.
     * 운영진 역할이 없는 경우 챌린저 공지 조건만 반환합니다.
     */
    private BooleanExpression buildCombinedCondition(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo
    ) {
        BooleanExpression challengerCondition =
            buildClassificationCondition(classification, viewerInfo.memberParts());

        Set<NoticeTargetRole> roles = viewerInfo.roles();
        if (roles == null || roles.isEmpty()) {
            return challengerCondition;
        }

        return challengerCondition.or(buildStaffCondition(classification, viewerInfo));
    }

    /**
     * 운영진 공지 조회 조건.
     * targetRoles의 CHALLENGER 미포함 여부로 운영진 공지를 판별하고,
     * viewerInfo.roles() overlap + 학교/파트 범위로 필터링합니다.
     */
    private BooleanExpression buildStaffCondition(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo
    ) {
        Set<NoticeTargetRole> roles = viewerInfo.roles();

        BooleanExpression gisuMatch = noticeTarget.targetGisuId.eq(classification.gisuId());
        BooleanExpression rolesMatch = buildRolesOverlapCondition(roles);

        // 학교 범위: 학교 미지정(기수 전체 대상) 공지 + 내 학교 공지
        BooleanExpression schoolMatch = viewerInfo.schoolId() != null
            ? noticeTarget.targetSchoolId.isNull().or(noticeTarget.targetSchoolId.eq(viewerInfo.schoolId()))
            : noticeTarget.targetSchoolId.isNull();

        // 파트 범위: 파트 미지정 공지 + 내 담당 파트 포함 공지
        BooleanExpression partMatch = targetPartIsEmptyOrContainsAny(viewerInfo.memberParts());

        return isNotChallengerNotice().and(gisuMatch).and(rolesMatch).and(schoolMatch).and(partMatch);
    }

    /**
     * viewerInfo.roles()와 공지의 targetRoles 간 overlap 조건을 생성합니다.
     */
    private BooleanExpression buildRolesOverlapCondition(Set<NoticeTargetRole> roles) {
        return roles.stream()
            .map(role -> Expressions.numberTemplate(Integer.class,
                "coalesce(array_position({0}, {1}), 0)",
                noticeTarget.targetRoles,
                Expressions.constant(role.name())
            ).gt(0))
            .reduce(BooleanExpression::or)
            .get();
    }

    /**
     * 조건에 맞는 공지 반환. 전체조회: keyword = null, 키워드조회: keyword = 검색어
     */
    private List<Notice> getContentQuery(BooleanExpression condition, String keyword, Pageable pageable) {
        return queryFactory
            .selectFrom(notice)
            .join(noticeTarget).on(noticeTarget.noticeId.eq(notice.id))
            .where(condition, keywordContains(keyword))
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 조건에 맞는 공지 총 개수. 전체조회: keyword = null, 키워드조회: keyword = 검색어
     */
    private Long getTotalCountQuery(BooleanExpression condition, String keyword) {
        return queryFactory
            .select(notice.count())
            .from(notice)
            .join(noticeTarget).on(noticeTarget.noticeId.eq(notice.id))
            .where(condition, keywordContains(keyword))
            .fetchOne();
    }

    /*
     * 전체조회 시 사용
     *
     * 조회 조건:
     * - 전체 조회 (gisuId만): 기수 전체 대상 공지 (지부/학교/파트 모두 NULL)
     * - 지부 조회 (gisuId + chapterId): 해당 지부 공지 + 전체 지부 대상 공지
     * - 학교 조회 (+ schoolId): 해당 학교 공지 + 전체 학교 대상 공지
     * - 파트 조회 (+ part): 해당 파트 공지 + 전체 파트 대상 공지
     */
    private BooleanExpression buildClassificationCondition(
        NoticeClassification classification,
        Set<ChallengerPart> memberParts
    ) {
        Long gisuId = classification.gisuId();
        Long chapterId = classification.chapterId();
        Long schoolId = classification.schoolId();
        ChallengerPart part = classification.part();

        boolean hasChapter = chapterId != null;
        boolean hasSchool = schoolId != null;
        boolean hasPart = part != null;

        if (gisuId == null) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING, "기수 ID는 필수입니다");
        }

        log.debug("공지사항 조회 조건 제작: gisuId={}, chapterId={}, schoolId={}, part={}, memberParts={}",
            gisuId, chapterId, schoolId, part, memberParts);

        // 챌린저 공지만 조회: target_staff_roles에 CHALLENGER가 포함된 경우만
        BooleanExpression challengerNoticeOnly = isChallengerNotice();

        // 특정 기수 공지 혹은 모든 기수 공지(targetGisuId=null)
        BooleanExpression gisuMatch = noticeTarget.targetGisuId.eq(gisuId)
            .or(noticeTarget.targetGisuId.isNull());

        // 전체 필터: 특정 기수 전체 대상 + 모든 기수 전체 대상(ALL_GISU_ALL_TARGET)
        if (!hasChapter && !hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(gisuMatch)
                .and(noticeTarget.targetChapterId.isNull())
                .and(noticeTarget.targetSchoolId.isNull())
                .and(targetPartIsEmpty());
        }

        // 지부 필터: 특정 기수 특정 지부 + (파트 없는 공지 OR 멤버 파트 공지)
        if (hasChapter && !hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(noticeTarget.targetGisuId.eq(gisuId))
                .and(noticeTarget.targetChapterId.eq(chapterId))
                .and(noticeTarget.targetSchoolId.isNull())
                .and(targetPartIsEmptyOrContainsAny(memberParts));
        }

        // 학교 필터: 특정 기수 특정 학교 + (파트 없는 공지 OR 멤버 파트 공지)
        if (!hasChapter && hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(gisuMatch)
                .and(noticeTarget.targetChapterId.isNull())
                .and(noticeTarget.targetSchoolId.eq(schoolId))
                .and(targetPartIsEmptyOrContainsAny(memberParts));
        }

        // 파트 필터: 기수+파트 / 기수+지부+파트 / 기수+학교+파트 세 패턴을 OR로 묶어 조회
        // chapterId, schoolId는 Service에서 호출자 소속 정보로 채워짐 (없으면 null)
        if (hasPart) {
            BooleanExpression gisuAndPartMatch = challengerNoticeOnly
                .and(noticeTarget.targetGisuId.eq(gisuId))
                .and(targetPartContains(part));

            // 특정 기수 + 특정 파트 (지부/학교 대상 없는 공지)
            BooleanExpression scopeCondition = noticeTarget.targetChapterId.isNull()
                .and(noticeTarget.targetSchoolId.isNull());

            // 특정 기수 + 특정 지부 + 특정 파트
            if (chapterId != null) {
                scopeCondition = scopeCondition.or(
                    noticeTarget.targetChapterId.eq(chapterId)
                        .and(noticeTarget.targetSchoolId.isNull())
                );
            }

            // 특정 기수 + 특정 학교 + 특정 파트
            if (schoolId != null) {
                scopeCondition = scopeCondition.or(
                    noticeTarget.targetChapterId.isNull()
                        .and(noticeTarget.targetSchoolId.eq(schoolId))
                );
            }

            return gisuAndPartMatch.and(scopeCondition);
        }

        // 그 외의 조건 조합은 허용되지 않음
        throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
            "현재 입력: gisuId=" + gisuId + ", chapterId=" + chapterId + ", schoolId=" + schoolId + ", part=" + part);
    }

    private BooleanExpression isChallengerNotice() {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(array_position({0}, {1}), 0)",
            noticeTarget.targetRoles,
            Expressions.constant(NoticeTargetRole.CHALLENGER.name())
        ).gt(0);
    }

    private BooleanExpression isNotChallengerNotice() {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(array_position({0}, {1}), 0)",
            noticeTarget.targetRoles,
            Expressions.constant(NoticeTargetRole.CHALLENGER.name())
        ).eq(0);
    }

    private BooleanExpression targetPartIsEmpty() {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(cardinality({0}), 0)",
            noticeTarget.targetChallengerPart
        ).eq(0);
    }

    private BooleanExpression targetPartContains(ChallengerPart part) {
        // HQL 파서가 = ANY(collection) 를 서브쿼리 한정자로 오해하므로
        // array_position 으로 대체 (NULL이면 미포함, 양수면 포함)
        return Expressions.numberTemplate(Integer.class,
            "coalesce(array_position({0}, {1}), 0)",
            noticeTarget.targetChallengerPart,
            Expressions.constant(part.name())
        ).gt(0);
    }

    /**
     * 파트 조건이 없는 공지 OR 멤버가 보유한 파트 중 하나라도 포함된 공지.
     * memberParts가 비어 있으면 파트 없는 공지만 반환합니다.
     */
    private BooleanExpression targetPartIsEmptyOrContainsAny(Set<ChallengerPart> memberParts) {
        BooleanExpression isEmpty = targetPartIsEmpty();
        if (memberParts == null || memberParts.isEmpty()) {
            return isEmpty;
        }
        BooleanExpression containsAny = memberParts.stream()
            .map(this::targetPartContains)
            .reduce(BooleanExpression::or)
            .get();

        return isEmpty.or(containsAny);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return notice.title.containsIgnoreCase(keyword)
            .or(notice.content.containsIgnoreCase(keyword));
    }
}
