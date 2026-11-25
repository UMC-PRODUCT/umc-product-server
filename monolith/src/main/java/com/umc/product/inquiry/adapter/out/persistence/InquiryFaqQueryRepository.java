package com.umc.product.inquiry.adapter.out.persistence;

import static com.umc.product.inquiry.domain.QInquiry.inquiry;
import static com.umc.product.inquiry.domain.QInquiryFaq.inquiryFaq;
import static com.umc.product.inquiry.domain.QInquiryFaqResolution.inquiryFaqResolution;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort.DemandTarget;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort.FaqDemand;
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

    public List<FaqDemand> rankByDistinctAuthors(
        DemandTarget target, LocalDate today, Instant since, Long categoryId, int limit
    ) {
        NumberExpression<Long> authors = inquiry.authorMemberId.countDistinct();
        DateTimeExpression<Instant> latest = inquiry.createdAt.max();
        return queryFactory.select(inquiry.faqId, authors, latest)
            .from(inquiry)
            .join(inquiryFaq).on(inquiryFaq.id.eq(inquiry.faqId))
            .where(
                inquiry.createdAt.goe(since),
                target == DemandTarget.PUBLIC ? publicOn(today) : inquiryFaq.status.eq(InquiryFaqStatus.DRAFT),
                categoryId == null ? null : categoryOf(target).eq(categoryId)
            )
            .groupBy(inquiry.faqId)
            .orderBy(authors.desc(), latest.desc(), inquiry.faqId.asc())
            .limit(limit)
            .fetch().stream()
            .map(tuple -> new FaqDemand(
                tuple.get(inquiry.faqId),
                tuple.get(authors),
                tuple.get(latest)
            ))
            .toList();
    }

    private static NumberPath<Long> categoryOf(DemandTarget target) {
        return target == DemandTarget.PUBLIC ? inquiryFaq.published.categoryId : inquiryFaq.draft.categoryId;
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
