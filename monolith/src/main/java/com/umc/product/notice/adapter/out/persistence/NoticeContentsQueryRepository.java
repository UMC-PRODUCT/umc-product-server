package com.umc.product.notice.adapter.out.persistence;

import static com.umc.product.notice.domain.QNoticeImage.noticeImage;
import static com.umc.product.notice.domain.QNoticeLink.noticeLink;
import static com.umc.product.notice.domain.QNoticeVote.noticeVote;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NoticeContentsQueryRepository {

    private final JPAQueryFactory queryFactory;

    public int findNextLinkDisplayOrder(Long noticeId) {
        Integer maxOrder = queryFactory
            .select(noticeLink.displayOrder.max())
            .from(noticeLink)
            .where(noticeLink.notice.id.eq(noticeId))
            .fetchOne();

        return maxOrder == null ? 0 : maxOrder + 1;
    }

    public int findNextImageDisplayOrder(Long noticeId) {
        Integer maxOrder = queryFactory
            .select(noticeImage.displayOrder.max())
            .from(noticeImage)
            .where(noticeImage.notice.id.eq(noticeId))
            .fetchOne();

        return maxOrder == null ? 0 : maxOrder + 1;
    }

    public List<Long> listNoticeIdsWithImages(List<Long> noticeIds) {
        if (noticeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(noticeImage.notice.id)
            .distinct()
            .from(noticeImage)
            .where(noticeImage.notice.id.in(noticeIds))
            .fetch();
    }

    public List<Long> listNoticeIdsWithLinks(List<Long> noticeIds) {
        if (noticeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(noticeLink.notice.id)
            .distinct()
            .from(noticeLink)
            .where(noticeLink.notice.id.in(noticeIds))
            .fetch();
    }

    public List<Long> listNoticeIdsWithVotes(List<Long> noticeIds) {
        if (noticeIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
            .select(noticeVote.notice.id)
            .distinct()
            .from(noticeVote)
            .where(noticeVote.notice.id.in(noticeIds))
            .fetch();
    }
}
