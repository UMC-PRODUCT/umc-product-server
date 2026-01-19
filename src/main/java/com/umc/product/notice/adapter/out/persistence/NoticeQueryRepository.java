package com.umc.product.notice.adapter.out.persistence;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.notice.domain.QNoticeRead;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeQueryRepository {

    private final JPAQueryFactory queryFactory;

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
}
