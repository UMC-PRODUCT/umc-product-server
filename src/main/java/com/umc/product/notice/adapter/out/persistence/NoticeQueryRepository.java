package com.umc.product.notice.adapter.out.persistence;

import com.querydsl.core.Tuple;
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

        List<Notice> content = getContentQuery(notice, target, condition, null, pageable);
        Long total = getTotalCountQuery(notice, target, condition, null);

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
        Long total = getTotalCountQuery(notice, target, condition, keyword);

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

    /**
     * 조건에 맞는 공지 반환 전체조회 : keyword = null, 키워드조회 : keyword = 검색어
     */
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
     * 조건에 맞는 공지 총 개수 전체조회 : keyword = null, 키워드조회 : keyword = 검색어
     */
    private Long getTotalCountQuery(QNotice notice, QNoticeTarget target,
                                    BooleanExpression condition, String keyword) {
        return queryFactory
            .select(notice.count())
            .from(notice)
            .join(target).on(target.noticeId.eq(notice.id))
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
        QNoticeTarget target) {

        Long gisuId = classification.gisuId();
        Long chapterId = classification.chapterId();
        Long schoolId = classification.schoolId();
        ChallengerPart part = classification.part();

        boolean hasChapter = chapterId != null;
        boolean hasSchool = schoolId != null;
        boolean hasPart = part != null;

        // 특정 기수 공지 혹은 모든 기수 공지(targetGisuId=null)
        BooleanExpression gisuMatch = target.targetGisuId.eq(gisuId)
            .or(target.targetGisuId.isNull());

        // 전체 필터: 특정 기수 전체 대상 + 모든 기수 전체 대상(ALL_GISU_ALL_TARGET)
        if (!hasChapter && !hasSchool && !hasPart) {
            return gisuMatch
                .and(target.targetChapterId.isNull())
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 지부 필터 (gisuId + chapterId): 해당 기수 지부 공지
        if (hasChapter && !hasSchool && !hasPart) {
            return target.targetGisuId.eq(gisuId)
                .and(target.targetChapterId.eq(chapterId))
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 학교 필터 (gisuId + chapterId + schoolId): 해당 기수 학교 대상 + 모든 기수 학교 대상 (ALL_GISU_SPECIFIC_SCHOOL)
        if (hasChapter && hasSchool && !hasPart) {
            return gisuMatch
                .and(target.targetChapterId.isNull())
                .and(target.targetSchoolId.eq(schoolId))
                .and(targetPartIsEmpty(target));
        }

        // 파트 필터 (gisuId + chapterId + schoolId + part)
        // 파트 공지는 기수 + 파트 / 기수 + 지부 + 파트 / 기수 + 학교 + 파트 세 패턴 묶기
        return target.targetGisuId.eq(gisuId)
            .and(targetPartContains(target, part))
            .and(
                target.targetChapterId.isNull().and(target.targetSchoolId.isNull())   // SPECIFIC_GISU_SPECIFIC_PART
                    .or(target.targetChapterId.eq(chapterId)
                        .and(target.targetSchoolId.isNull()))  // SPECIFIC_GISU_SPECIFIC_CHAPTER_WITH_PART
                    .or(target.targetChapterId.isNull()
                        .and(target.targetSchoolId.eq(schoolId))) // SPECIFIC_GISU_SPECIFIC_SCHOOL_WITH_PART
            );
    }

    /*
     * 검색어 기반 전체조회 시 사용
     * 해당 조회는 필터링 없이사용자가 접근 가능한 모든 공지 범위(전체/지부/학교/파트)에서
     * 키워드를 검색하므로 condition 빌더 별도 구현
     */
    private BooleanExpression buildClassificationConditionForKeywordSearch(
        NoticeClassification classification,
        QNoticeTarget target) {

        Long gisuId = classification.gisuId();
        Long chapterId = classification.chapterId();
        Long schoolId = classification.schoolId();
        ChallengerPart part = classification.part();

        // 특정 기수 공지 OR 모든 기수 공지(targetGisuId=null)
        BooleanExpression gisuMatch = target.targetGisuId.eq(gisuId)
            .or(target.targetGisuId.isNull());

        // 전체 공지: 특정 기수 전체 대상 + 모든 기수 전체 대상(ALL_GISU_ALL_TARGET)
        BooleanExpression allScope = gisuMatch
            .and(target.targetChapterId.isNull())
            .and(target.targetSchoolId.isNull())
            .and(targetPartIsEmpty(target));

        // 지부 공지: ALL_GISU_WITH_CHAPTER는 불가한 패턴이므로 특정 기수 지부 대상만
        BooleanExpression chapterScope = null;
        if (chapterId != null) {
            chapterScope = target.targetGisuId.eq(gisuId)
                .and(target.targetChapterId.eq(chapterId))
                .and(target.targetSchoolId.isNull())
                .and(targetPartIsEmpty(target));
        }

        // 학교 공지: 특정 기수 특정 학교 대상 + 모든 기수 특정 학교 대상(ALL_GISU_SPECIFIC_SCHOOL)
        BooleanExpression schoolScope = null;
        if (schoolId != null) {
            schoolScope = gisuMatch
                .and(target.targetChapterId.isNull())
                .and(target.targetSchoolId.eq(schoolId))
                .and(targetPartIsEmpty(target));
        }

        // 파트 공지: 기수+파트 / 기수+지부+파트 / 기수+학교+파트 세 패턴 OR 조합
        BooleanExpression partScope = null;
        if (chapterId != null && schoolId != null && part != null) {
            partScope = target.targetGisuId.eq(gisuId)
                .and(targetPartContains(target, part))
                .and(
                    target.targetChapterId.isNull().and(target.targetSchoolId.isNull())
                        .or(target.targetChapterId.eq(chapterId).and(target.targetSchoolId.isNull()))
                        .or(target.targetChapterId.isNull().and(target.targetSchoolId.eq(schoolId)))
                );
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

    private BooleanExpression targetPartIsEmpty(QNoticeTarget target) {
        return Expressions.numberTemplate(Integer.class,
            "coalesce(cardinality({0}), 0)",
            target.targetChallengerPart
        ).eq(0);
    }

    private BooleanExpression targetPartContains(QNoticeTarget target, ChallengerPart part) {
        return Expressions.booleanTemplate(
            "array_contains({0}, {1})",
            target.targetChallengerPart,
            part
        );
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
