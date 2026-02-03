package com.umc.product.notice.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.QNotice;
import com.umc.product.notice.domain.QNoticeRead;
import com.umc.product.notice.domain.QNoticeTarget;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.application.port.out.NoticeTargetCondition;
import java.util.List;
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
     * 분류 (중앙, 지부, 학교, 파트) 별 조회
     * @return Notice 페이지
     * */
    public Page<Notice> findByClassification(
        NoticeClassification classification,
        Pageable pageable) {

        QNotice notice = QNotice.notice;

        // 데이터 조회
        List<Notice> content = queryFactory
            .selectFrom(notice)
            // TODO: NoticePermission과 연계해서 검색할 것
//            .where(
//                notice.scope.eq(classification)
//            )
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 개수 조회
        Long total = queryFactory
            .select(notice.count())
            .from(notice)
//            .where(
//                notice.scope.eq(classification)
//            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /*
     * 검색어 기반 조회
     * @return Notice 페이지
     * */
    public Page<Notice> findByKeyword(String keyword, Pageable pageable) {
        QNotice notice = QNotice.notice;

        List<Notice> notices = queryFactory
            .selectFrom(notice)
            .where(
                keywordContains(keyword)
            )
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(notice.count())
            .from(notice)
            .where(
                keywordContains(keyword)
            )
            .fetchOne();

        return new PageImpl<>(notices, pageable, total != null ? total : 0L);
    }

    /**
     * 사용자 대상 공지 조회 (정확한 페이지네이션)
     * <p>조회자의 gisu/chapter/part 조합과 schoolId로 NoticeTarget을 매칭하고,
     * classification 조건까지 DB에서 필터링한 뒤 페이지네이션을 적용합니다.</p>
     *
     * @return 조회자에게 노출 가능한 공지 페이지
     */
    public Page<Notice> findVisibleNotices(
        Long schoolId,
        List<NoticeTargetCondition> conditions,
        NoticeClassification classification,
        Pageable pageable) {

        if (conditions == null || conditions.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        QNotice notice = QNotice.notice;
        QNoticeTarget target = QNoticeTarget.noticeTarget;

        BooleanExpression visibility = null;
        for (NoticeTargetCondition condition : conditions) {
            BooleanExpression match = target.targetGisuId.eq(condition.gisuId())
                .and(target.targetChapterId.isNull().or(target.targetChapterId.eq(condition.chapterId())))
                .and(target.targetSchoolId.isNull().or(target.targetSchoolId.eq(schoolId)))
                .and(partMatch(target, condition));

            visibility = (visibility == null) ? match : visibility.or(match);
        }

        BooleanExpression classificationFilter = classificationFilter(target, classification);
        BooleanExpression whereClause = (classificationFilter == null)
            ? visibility
            : visibility.and(classificationFilter);

        List<Notice> content = queryFactory
            .selectFrom(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(whereClause)
            .orderBy(notice.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(notice.count())
            .from(notice)
            .join(target).on(target.noticeId.eq(notice.id))
            .where(whereClause)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;  // 조건 무시
        }

        QNotice notice = QNotice.notice;

        return notice.title.containsIgnoreCase(keyword)
            .or(notice.content.containsIgnoreCase(keyword));
    }

    /**
     * NoticeClassification을 NoticeTarget 기반 필터로 변환합니다.
     * <p>NoticeClassification은 Notice에 직접 저장되지 않으므로, 타겟 컬럼을 기준으로 추론합니다.</p>
     *
     * @return 분류 필터 표현식 (없으면 null)
     */
    private BooleanExpression classificationFilter(QNoticeTarget target, NoticeClassification classification) {
        if (classification == null) {
            return null;
        }
        return switch (classification) {
            case PART -> partSpecified(target);
            case CHAPTER -> target.targetChapterId.isNotNull();
            case SCHOOL -> target.targetSchoolId.isNotNull();
            case MANAGER_NOTI -> target.targetChapterId.isNull().and(target.targetSchoolId.isNull());
        };
    }

    /**
     * 조회자의 part가 타겟 part 조건에 부합하는지 확인합니다.
     * 타겟 part가 null/empty이면 무조건 매칭으로 간주합니다.
     *
     * @return part 매칭 조건식
     */
    private BooleanExpression partMatch(QNoticeTarget target, NoticeTargetCondition condition) {
        String part = condition.part().name();
        return Expressions.booleanTemplate(
            "({0} is null or cardinality({0}) = 0 or {1} = any({0}))",
            target.targetChallengerPart,
            part
        );
    }

    /**
     * 타겟 part가 명시되어 있는지(비어있지 않은지) 확인합니다.
     *
     * @return 타겟 part가 지정된 경우 true
     */
    private BooleanExpression partSpecified(QNoticeTarget target) {
        return Expressions.booleanTemplate(
            "({0} is not null and cardinality({0}) > 0)",
            target.targetChallengerPart
        );
    }
}
