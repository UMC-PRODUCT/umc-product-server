package com.umc.product.inquiry.adapter.out.persistence;

import static com.umc.product.inquiry.domain.QInquiry.inquiry;
import static com.umc.product.inquiry.domain.QInquiryLabelAssignment.inquiryLabelAssignment;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort.Cursor;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort.StaffSearch;
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

    public List<Inquiry> searchForStaff(StaffSearch search, Cursor cursor, int limit) {
        return queryFactory.selectFrom(inquiry)
            .where(staffFilters(search), after(cursor))
            .orderBy(inquiry.lastMessageAt.desc(), inquiry.id.desc())
            .limit(limit)
            .fetch();
    }

    public long countForStaff(StaffSearch search) {
        Long count = queryFactory.select(inquiry.count())
            .from(inquiry)
            .where(staffFilters(search))
            .fetchOne();
        return count == null ? 0 : count;
    }

    private BooleanExpression staffFilters(StaffSearch search) {
        return Expressions.allOf(
            tab(search),
            search.statuses().isEmpty() ? null : inquiry.status.in(search.statuses()),
            search.categoryIds().isEmpty() ? null : inquiry.categoryId.in(search.categoryIds()),
            search.unclassifiedOnly() ? inquiry.faqId.isNull() : null,
            search.labelIds().isEmpty() ? null : inquiry.id.in(
                JPAExpressions.select(inquiryLabelAssignment.inquiryId)
                    .from(inquiryLabelAssignment)
                    .where(inquiryLabelAssignment.labelId.in(search.labelIds()))
            )
        );
    }

    private BooleanExpression tab(StaffSearch search) {
        Set<Long> staff = search.staffMemberIds();
        return switch (search.tab()) {
            case ALL -> null;
            case MINE -> staff.contains(search.viewerMemberId())
                ? inquiry.assigneeMemberId.eq(search.viewerMemberId())
                : inquiry.id.isNull();
            case UNASSIGNED -> staff.isEmpty()
                ? null
                : inquiry.assigneeMemberId.isNull().or(inquiry.assigneeMemberId.notIn(staff));
        };
    }

    private BooleanExpression after(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiry.lastMessageAt.lt(cursor.lastMessageAt())
            .or(inquiry.lastMessageAt.eq(cursor.lastMessageAt()).and(inquiry.id.lt(cursor.inquiryId())));
    }
}
