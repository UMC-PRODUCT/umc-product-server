package com.umc.product.inquiry.adapter.out.persistence;

import static com.umc.product.inquiry.domain.QInquiryFaq.inquiryFaq;
import static com.umc.product.inquiry.domain.QInquiryFaqResolution.inquiryFaqResolution;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InquiryFaqQueryRepository {

    private final JPAQueryFactory queryFactory;

    /** {@link InquiryFaq#isPublicOn}과 같은 조건이다. */
    static BooleanExpression publicOn(LocalDate today) {
        return inquiryFaq.status.eq(InquiryFaqStatus.PUBLISHED)
            .and(inquiryFaq.published.endDate.isNull().or(inquiryFaq.published.endDate.goe(today)));
    }

    public List<InquiryFaq> listForStaff(InquiryFaqStatus status, Long categoryId) {
        return queryFactory.selectFrom(inquiryFaq)
            .where(
                status == null ? null : inquiryFaq.status.eq(status),
                categoryId == null ? null : inquiryFaq.draft.categoryId.eq(categoryId)
            )
            .orderBy(inquiryFaq.updatedAt.desc(), inquiryFaq.id.desc())
            .fetch();
    }

    public List<InquiryFaq> listPublic(Long categoryId, LocalDate today) {
        return queryFactory.selectFrom(inquiryFaq)
            .where(publicOn(today), categoryId == null ? null : inquiryFaq.published.categoryId.eq(categoryId))
            .orderBy(inquiryFaq.publishedAt.desc(), inquiryFaq.id.desc())
            .fetch();
    }

    public List<InquiryFaq> searchPublic(String keyword, LocalDate today, int limit) {
        return queryFactory.selectFrom(inquiryFaq)
            .where(publicOn(today), inquiryFaq.publishedSearchText.containsIgnoreCase(keyword))
            .orderBy(inquiryFaq.publishedAt.desc(), inquiryFaq.id.desc())
            .limit(limit)
            .fetch();
    }

    public Map<Long, Long> countResolutionsByFaqIds(Collection<Long> faqIds) {
        if (faqIds.isEmpty()) {
            return Map.of();
        }
        return queryFactory.select(inquiryFaqResolution.faqId, inquiryFaqResolution.count())
            .from(inquiryFaqResolution)
            .where(inquiryFaqResolution.faqId.in(faqIds))
            .groupBy(inquiryFaqResolution.faqId)
            .fetch().stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(inquiryFaqResolution.faqId),
                tuple -> tuple.get(inquiryFaqResolution.count())
            ));
    }
}
