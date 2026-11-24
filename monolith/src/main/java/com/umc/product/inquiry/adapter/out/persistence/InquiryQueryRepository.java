package com.umc.product.inquiry.adapter.out.persistence;

import static com.umc.product.inquiry.domain.QInquiry.inquiry;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort.Cursor;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InquiryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Inquiry> listByAuthor(Long authorMemberId, Set<InquiryStatus> statuses, Cursor cursor, int limit) {
        return queryFactory.selectFrom(inquiry)
            .where(
                inquiry.authorMemberId.eq(authorMemberId),
                inquiry.status.in(statuses),
                after(cursor)
            )
            .orderBy(inquiry.lastMessageAt.desc(), inquiry.id.desc())
            .limit(limit)
            .fetch();
    }

    private BooleanExpression after(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiry.lastMessageAt.lt(cursor.lastMessageAt())
            .or(inquiry.lastMessageAt.eq(cursor.lastMessageAt()).and(inquiry.id.lt(cursor.inquiryId())));
    }
}
