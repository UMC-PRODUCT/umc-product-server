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


    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;  // 조건 무시
        }

        QNotice notice = QNotice.notice;

        return notice.title.containsIgnoreCase(keyword)
            .or(notice.content.containsIgnoreCase(keyword));
    }
}
