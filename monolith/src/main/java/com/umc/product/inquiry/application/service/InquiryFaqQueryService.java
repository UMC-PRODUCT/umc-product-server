package com.umc.product.inquiry.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqForStaffUseCase;
import com.umc.product.inquiry.application.port.in.query.GetInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqAudienceInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqRefInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffSummaryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqSummaryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffMemberInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqAudiencePort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort.DemandTarget;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqContent;
import com.umc.product.inquiry.domain.enums.InquiryFaqRankingPeriod;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryFaqQueryService implements GetInquiryFaqForStaffUseCase, GetInquiryFaqUseCase {

    static final int SEARCH_LIMIT = 20;
    static final InquiryFaqRankingPeriod SUGGESTION_PERIOD = InquiryFaqRankingPeriod.WEEK;

    private final LoadInquiryFaqPort loadInquiryFaqPort;
    private final LoadInquiryFaqAudiencePort loadInquiryFaqAudiencePort;
    private final InquiryFaqAssembler faqAssembler;
    private final GetMemberUseCase getMemberUseCase;
    private final InquiryAccessPolicy accessPolicy;
    private final Clock clock;

    @Override
    public List<InquiryFaqStaffSummaryInfo> listForStaff(Long staffMemberId, InquiryFaqStatus status, Long categoryId) {
        accessPolicy.requireStaff(staffMemberId);
        List<InquiryFaq> faqs = loadInquiryFaqPort.listForStaff(status, categoryId);
        InquiryFaqAssembler.Lookup lookup = faqAssembler.lookup(faqs.stream().map(InquiryFaq::getDraft).toList());
        Map<Long, Long> resolved = loadInquiryFaqPort.countResolutionsByFaqIds(
            faqs.stream().map(InquiryFaq::getId).collect(Collectors.toSet())
        );
        return faqs.stream()
            .map(faq -> new InquiryFaqStaffSummaryInfo(
                faq.getId(),
                faq.getVersion(),
                faq.getStatus(),
                faq.getDraft().getQuestion(),
                lookup.category(faq.getDraft().getCategoryId()),
                faq.hasUnpublishedChanges(),
                faq.getPublishedAt(),
                resolved.getOrDefault(faq.getId(), 0L),
                faq.getUpdatedAt()
            ))
            .toList();
    }

    @Override
    public InquiryFaqStaffDetailInfo getForStaff(Long staffMemberId, Long faqId) {
        accessPolicy.requireStaff(staffMemberId);
        InquiryFaq faq = loadInquiryFaqPort.findById(faqId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND));
        List<InquiryFaqContent> contents = new ArrayList<>();
        contents.add(faq.getDraft());
        contents.add(faq.getPublished());
        InquiryFaqAssembler.Lookup lookup = faqAssembler.lookup(contents);
        Long publisherId = faq.getPublishedByMemberId();
        return new InquiryFaqStaffDetailInfo(
            faq.getId(),
            faq.getVersion(),
            faq.getStatus(),
            faq.hasUnpublishedChanges(),
            lookup.staffContent(faq.getDraft()),
            lookup.staffContent(faq.getPublished()),
            faq.getPublishedAt(),
            publisherId == null ? null : new InquiryStaffMemberInfo(
                publisherId, getMemberUseCase.findAllNamesByIds(Set.of(publisherId)).get(publisherId)
            ),
            InquiryFaq.VERIFIED_BY,
            loadInquiryFaqPort.countResolutionsByFaqIds(Set.of(faqId)).getOrDefault(faqId, 0L)
        );
    }

    @Override
    public List<InquiryFaqAudienceInfo> listAudiences(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        return loadInquiryFaqAudiencePort.listActive().stream().map(InquiryFaqAudienceInfo::from).toList();
    }

    @Override
    public List<InquiryFaqSummaryInfo> listPublic(Long categoryId) {
        return faqAssembler.publicSummaries(loadInquiryFaqPort.listPublic(categoryId, clock.instant()));
    }

    @Override
    public InquiryFaqDetailInfo getPublic(Long faqId) {
        Instant now = clock.instant();
        InquiryFaq faq = loadInquiryFaqPort.findById(faqId)
            .filter(found -> found.isPublicAt(now))
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND));
        InquiryFaqContent content = faq.getPublished();
        InquiryFaqAssembler.Lookup lookup = faqAssembler.lookup(List.of(content));
        InquiryFaqSummaryInfo summary = lookup.summary(faq.getId(), content);
        InquiryFaq related = content.getRelatedFaqId() == null ? null : lookup.related().get(content.getRelatedFaqId());
        return new InquiryFaqDetailInfo(
            faq.getId(),
            content.getQuestion(),
            content.getAnswer(),
            summary.category(),
            summary.audience(),
            InquiryFaq.VERIFIED_BY,
            faq.getPublishedAt(),
            related != null && related.isPublicAt(now)
                ? new InquiryFaqRefInfo(related.getId(), related.getPublished().getQuestion())
                : null
        );
    }

    @Override
    public List<InquiryFaqSummaryInfo> search(String keyword) {
        String trimmed = keyword == null ? "" : keyword.strip();
        if (trimmed.isEmpty()) {
            return List.of();
        }
        return faqAssembler.publicSummaries(loadInquiryFaqPort.searchPublic(trimmed, clock.instant(), SEARCH_LIMIT));
    }

    @Override
    public Optional<InquiryFaqSummaryInfo> suggest(Long categoryId) {
        Instant now = clock.instant();
        Optional<InquiryFaq> mostAsked = loadInquiryFaqPort.rankByDistinctAuthors(
                DemandTarget.PUBLIC, now, SUGGESTION_PERIOD.since(now), categoryId, 1
            ).stream()
            .findFirst()
            .flatMap(demand -> loadInquiryFaqPort.findById(demand.faqId()));
        return mostAsked.or(() -> loadInquiryFaqPort.listPublic(categoryId, now).stream().findFirst())
            .map(faq -> faqAssembler.publicSummaries(List.of(faq)).get(0));
    }
}
