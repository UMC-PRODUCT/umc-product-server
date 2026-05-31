package com.umc.product.notice.adapter.out.persistence;

import static com.umc.product.notice.domain.QNoticeImage.noticeImage;
import static com.umc.product.notice.domain.QNoticeLink.noticeLink;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
}
