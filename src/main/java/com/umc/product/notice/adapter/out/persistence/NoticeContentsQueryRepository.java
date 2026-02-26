package com.umc.product.notice.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.notice.domain.QNoticeImage;
import com.umc.product.notice.domain.QNoticeLink;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NoticeContentsQueryRepository {

    private final JPAQueryFactory queryFactory;

    public int findNextLinkDisplayOrder(Long noticeId) {
        QNoticeLink link = QNoticeLink.noticeLink;

        Integer maxOrder = queryFactory
            .select(link.displayOrder.max())
            .from(link)
            .where(link.notice.id.eq(noticeId))
            .fetchOne();

        return maxOrder == null ? 0 : maxOrder + 1;
    }

    public int findNextImageDisplayOrder(Long noticeId) {
        QNoticeImage image = QNoticeImage.noticeImage;

        Integer maxOrder = queryFactory
            .select(image.displayOrder.max())
            .from(image)
            .where(image.notice.id.eq(noticeId))
            .fetchOne();

        return maxOrder == null ? 0 : maxOrder + 1;
    }
}
