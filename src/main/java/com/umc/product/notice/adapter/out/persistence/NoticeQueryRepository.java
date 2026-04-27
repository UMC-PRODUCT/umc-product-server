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

    public Page<Notice> findByClassification(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo,
        Pageable pageable) {

        BooleanExpression condition = buildCombinedCondition(classification, viewerInfo);
        List<Notice> content = getContentQuery(condition, null, pageable);
        Long total = getTotalCountQuery(condition, null);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

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

    private BooleanExpression buildCombinedCondition(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo
    ) {
        if (classification.isChallengerQuery()) {
            return buildClassificationCondition(classification, viewerInfo.memberParts());
        }
        return buildStaffCondition(classification, viewerInfo);
    }

    /**
     * 운영진 공지 조회 조건.
     * minTargetRole 하한선 방식으로, viewerRole이 읽을 수 있는 역할 목록을 IN 조건으로 처리합니다.
     */
    private BooleanExpression buildStaffCondition(
        NoticeClassification classification,
        NoticeViewerInfo viewerInfo
    ) {
        NoticeTargetRole viewerRole = viewerInfo.viewerRole();
        List<NoticeTargetRole> readableRoles = NoticeTargetRole.staffRolesReadableBy(viewerRole);

        BooleanExpression gisuMatch = noticeTarget.targetGisuId.eq(classification.gisuId());
        BooleanExpression rolesMatch = noticeTarget.minTargetRole.in(readableRoles);
        BooleanExpression partMatch = buildStaffPartCondition(classification, viewerInfo);

        // schoolId 있음 → 교내공지: 해당 학교 공지만 / 없음 → 중앙공지: school null만 (총괄/중운도 동일)
        BooleanExpression schoolMatch = classification.schoolId() != null
            ? noticeTarget.targetSchoolId.eq(classification.schoolId())
            : noticeTarget.targetSchoolId.isNull();

        return isNotChallengerNotice().and(gisuMatch).and(rolesMatch).and(schoolMatch).and(partMatch);
    }

    /**
     * 운영진 공지 파트 조건.
     * - classification.part() 명시 시: 해당 파트 또는 파트 미지정 공지
     * - 회장단 이상(CENTRAL_MEMBER, SCHOOL_CORE): 파트 미지정 시 전체 파트 조회
     * - 파트장: 담당 파트로만 필터링
     */
    private BooleanExpression buildStaffPartCondition(NoticeClassification classification, NoticeViewerInfo viewerInfo) {
        // 파트 지정 시: 역할 무관하게 해당 파트 공지만 (파트 미지정 공지 제외)
        if (classification.part() != null) {
            return targetPartContains(classification.part());
        }
        NoticeTargetRole viewerRole = viewerInfo.viewerRole();
        // 총괄/중운/회장단 파트 미지정: 전체 보임
        if (viewerRole == NoticeTargetRole.CENTRAL_MEMBER || viewerRole == NoticeTargetRole.SCHOOL_CORE) {
            return Expressions.TRUE;
        }
        // 파트장 파트 미지정: 담당 파트 + 파트 미지정 공지
        return targetPartIsEmptyOrContainsAny(viewerInfo.memberParts());
    }

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

        BooleanExpression challengerNoticeOnly = isChallengerNotice();
        BooleanExpression gisuMatch = noticeTarget.targetGisuId.eq(gisuId)
            .or(noticeTarget.targetGisuId.isNull());

        if (!hasChapter && !hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(gisuMatch)
                .and(noticeTarget.targetChapterId.isNull())
                .and(noticeTarget.targetSchoolId.isNull())
                .and(targetPartIsEmpty());
        }

        if (hasChapter && !hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(noticeTarget.targetGisuId.eq(gisuId))
                .and(noticeTarget.targetChapterId.eq(chapterId))
                .and(noticeTarget.targetSchoolId.isNull())
                .and(targetPartIsEmptyOrContainsAny(memberParts));
        }

        if (!hasChapter && hasSchool && !hasPart) {
            return challengerNoticeOnly
                .and(gisuMatch)
                .and(noticeTarget.targetChapterId.isNull())
                .and(noticeTarget.targetSchoolId.eq(schoolId))
                .and(targetPartIsEmptyOrContainsAny(memberParts));
        }

        if (hasPart) {
            BooleanExpression gisuAndPartMatch = challengerNoticeOnly
                .and(noticeTarget.targetGisuId.eq(gisuId))
                .and(targetPartContains(part));

            BooleanExpression scopeCondition = noticeTarget.targetChapterId.isNull()
                .and(noticeTarget.targetSchoolId.isNull());

            if (chapterId != null) {
                scopeCondition = scopeCondition.or(
                    noticeTarget.targetChapterId.eq(chapterId)
                        .and(noticeTarget.targetSchoolId.isNull())
                );
            }

            if (schoolId != null) {
                scopeCondition = scopeCondition.or(
                    noticeTarget.targetChapterId.isNull()
                        .and(noticeTarget.targetSchoolId.eq(schoolId))
                );
            }

            return gisuAndPartMatch.and(scopeCondition);
        }

        throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_SETTING,
            "현재 입력: gisuId=" + gisuId + ", chapterId=" + chapterId + ", schoolId=" + schoolId + ", part=" + part);
    }

    private BooleanExpression isChallengerNotice() {
        return noticeTarget.minTargetRole.eq(NoticeTargetRole.CHALLENGER);
    }

    private BooleanExpression isNotChallengerNotice() {
        return noticeTarget.minTargetRole.ne(NoticeTargetRole.CHALLENGER);
    }

    private BooleanExpression targetPartIsEmpty() {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(cardinality({0}), 0)",
            noticeTarget.targetChallengerPart
        ).eq(0);
    }

    private BooleanExpression targetPartContains(ChallengerPart part) {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(array_position({0}, {1}), 0)",
            noticeTarget.targetChallengerPart,
            Expressions.constant(part.name())
        ).gt(0);
    }

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

    private Long getTotalCountQuery(BooleanExpression condition, String keyword) {
        return queryFactory
            .select(notice.count())
            .from(notice)
            .join(noticeTarget).on(noticeTarget.noticeId.eq(notice.id))
            .where(condition, keywordContains(keyword))
            .fetchOne();
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return notice.title.containsIgnoreCase(keyword)
            .or(notice.content.containsIgnoreCase(keyword));
    }
}
