package com.umc.product.notice.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.QNotice;
import com.umc.product.notice.domain.QNoticeRead;
import com.umc.product.notice.domain.QNoticeTarget;
import com.umc.product.notice.dto.NoticeClassification;
import com.querydsl.core.Tuple;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;

    /*
     *  안 읽은 챌린저 조회
     * @return 안 읽은 챌린저 ID 리스트
     */
    public List<Long> findUnreadChallengerIdByNoticeId(Long noticeId) {
        QNoticeRead noticeRead = QNoticeRead.noticeRead;
        QChallenger challenger = QChallenger.challenger;

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
        Pageable pageable) {

        QNotice notice = QNotice.notice;
        QNoticeTarget target = QNoticeTarget.noticeTarget;

        BooleanExpression condition = buildClassificationCondition(classification, target);

        List<Notice> content = getContentQuery(notice, target, condition, pageable);
        Long total = getTotalCountQuery(notice, target, condition);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /*
     * 검색어 기반 조회
     * @return Notice 페이지
     * */
    public Page<Notice> findByKeyword(
        String keyword,
        NoticeClassification classification,
        Pageable pageable) {

        QNotice notice = QNotice.notice;
        QNoticeTarget target = QNoticeTarget.noticeTarget;

        BooleanExpression condition = buildClassificationConditionForKeywordSearch(classification, target);

        List<Notice> notices = getContentQuery(notice, target, condition, keyword, pageable);
        Long total = getTotalCountQuery(notice, target, keyword, condition);

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }


    /*
     * 여러 공지사항의 읽은 사람 수를 한 번에 조회
     * @return noticeId → 읽음 수 Map
     */
    public Map<Long, Long> countReadsByNoticeIds(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Map.of();
        }

        QNoticeRead noticeRead = QNoticeRead.noticeRead;

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

    // 조건에 맞는 공지 총 개수
    private Long getTotalCountQuery(QNotice notice, QNoticeTarget target, BooleanExpression condition) {
        return queryFactory
            .select(notice.count())
            .from(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(condition)
            .fetchOne();
    }

    // 조건에 맞는 공지 총 개수 - 검색어 기반 조회용
    private Long getTotalCountQuery(QNotice notice, QNoticeTarget target, String keyword, BooleanExpression condition) {
        return queryFactory
            .select(notice.count())
            .from(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(condition, keywordContains(keyword))
            .fetchOne();
    }

    // 조건에 맞는 공지 반환
    private List<Notice> getContentQuery(QNotice notice, QNoticeTarget target,
                                         BooleanExpression condition, Pageable pageable) {

        return queryFactory
            .selectFrom(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(condition)
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    // 조건에 맞는 공지 반환 - 검색어 기반 조회용
    private List<Notice> getContentQuery(QNotice notice, QNoticeTarget target,
                                         BooleanExpression condition, String keyword, Pageable pageable) {

        return queryFactory
            .selectFrom(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(condition,
                keywordContains(keyword))
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 검색어 기반 전체조회 시 사용
     * 해당 조회는 전체/지부/학교/파트에 해당하는 모든 조회 중, 키워드가 들어있는 공지를 반환해야 하므로
     * condition 빌더 별도 구현
     */
    private BooleanExpression buildClassificationConditionForKeywordSearch(
        NoticeClassification classification,
        QNoticeTarget target) {

        Long gisuId = classification.gisuId();
        Long chapterId = classification.chapterId();
        Long schoolId = classification.schoolId();
        ChallengerPart part = classification.part();

        // 기수 조건은 항상 고정
        BooleanExpression gisuCondition = target.targetGisuId.eq(gisuId);

        // 전체 공지 (기수 전체)
        BooleanExpression allScope = gisuCondition
            .and(target.targetChapterId.isNull())
            .and(target.targetSchoolId.isNull())
            .and(targetPartIsEmpty(target));

        // 지부 공지 (해당 지부 + 전체 지부)
        BooleanExpression chapterScope = null;
        if (chapterId != null) {
            chapterScope = gisuCondition
                .and(target.targetChapterId.eq(chapterId).or(target.targetChapterId.isNull()))
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 학교 공지 (해당 학교 + 전체 학교)
        BooleanExpression schoolScope = null;
        if (chapterId != null && schoolId != null) {
            schoolScope = gisuCondition
                .and(target.targetChapterId.eq(chapterId))
                .and(target.targetSchoolId.eq(schoolId).or(target.targetSchoolId.isNull()))
                .and(targetPartIsEmpty(target));
        }

        // 파트 공지 (해당 파트 + 전체 파트)
        BooleanExpression partScope = null;
        if (chapterId != null && schoolId != null && part != null) {
            partScope = gisuCondition
                .and(target.targetChapterId.eq(chapterId))
                .and(target.targetSchoolId.eq(schoolId))
                .and(targetPartContainsOrEmpty(target, part));
        }

        BooleanExpression result = allScope;
        if (chapterScope != null) {
            result = result.or(chapterScope);
        }
        if (schoolScope != null) {
            result = result.or(schoolScope);
        }
        if (partScope != null) {
            result = result.or(partScope);
        }

        return result;
    }

    private BooleanExpression buildClassificationCondition(
        NoticeClassification classification,
        QNoticeTarget target) {

        Long gisuId = classification.gisuId();
        Long chapterId = classification.chapterId();
        Long schoolId = classification.schoolId();
        ChallengerPart part = classification.part();

        // 기수 조건은 항상 고정
        BooleanExpression condition = target.targetGisuId.eq(gisuId);

        // 전체 조회: gisuId만 있고 나머지 null
        // → 기수 전체 대상 공지 (지부/학교/파트 모두 NULL인 공지)
        if (chapterId == null) {
            return condition
                .and(target.targetChapterId.isNull())
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 지부 조회: gisuId + chapterId 있고, schoolId/part null
        // → 해당 지부 공지 + 전체 지부 대상 공지 (targetChapterId가 null이거나 일치)
        if (schoolId == null) {
            return condition
                .and(target.targetChapterId.eq(chapterId).or(target.targetChapterId.isNull()))
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 학교 조회: gisuId + chapterId + schoolId 있고, part null
        // → 해당 학교 공지 + 전체 학교 대상 공지 (targetSchoolId가 null이거나 일치)
        if (part == null) {
            return condition
                .and(target.targetChapterId.eq(chapterId))
                .and(target.targetSchoolId.eq(schoolId).or(target.targetSchoolId.isNull()))
                .and(targetPartIsEmpty(target));
        }

        // 파트 조회: 모든 필드 있음
        // → 해당 파트 공지 + 전체 파트 대상 공지 (targetPart가 비어있거나 포함)
        return condition
            .and(target.targetChapterId.eq(chapterId))
            .and(target.targetSchoolId.eq(schoolId))
            .and(targetPartContainsOrEmpty(target, part));
    }

    private BooleanExpression targetPartIsEmpty(QNoticeTarget target) {
        return target.targetChallengerPart.isEmpty();
    }

    private BooleanExpression targetPartContainsOrEmpty(QNoticeTarget target, ChallengerPart part) {
        return targetPartIsEmpty(target)
            .or(Expressions.booleanTemplate(
                "{0} @> ARRAY[{1}]::varchar[]",
                target.targetChallengerPart,
                part.name()
            ));
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;  // 조건 무시
        }

        QNotice notice = QNotice.notice;

        return notice.title.containsIgnoreCase(keyword)
            .or(notice.content.containsIgnoreCase(keyword));
    }

}
