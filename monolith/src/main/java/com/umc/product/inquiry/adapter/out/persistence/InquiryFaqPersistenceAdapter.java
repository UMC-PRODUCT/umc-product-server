package com.umc.product.inquiry.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryFaqPort;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryFaqPersistenceAdapter implements LoadInquiryFaqPort, SaveInquiryFaqPort {

    private final InquiryFaqJpaRepository faqRepository;
    private final InquiryFaqResolutionJpaRepository resolutionRepository;
    private final InquiryFaqQueryRepository queryRepository;

    @Override
    public Optional<InquiryFaq> findById(Long faqId) {
        return faqRepository.findById(faqId);
    }

    @Override
    public List<InquiryFaq> listByIds(Collection<Long> faqIds) {
        return faqIds.isEmpty() ? List.of() : faqRepository.findAllById(faqIds);
    }

    @Override
    public List<InquiryFaq> listForStaff(InquiryFaqStatus status, Long categoryId) {
        return queryRepository.listForStaff(status, categoryId);
    }

    @Override
    public List<InquiryFaq> listPublic(Long categoryId, LocalDate today) {
        return queryRepository.listPublic(categoryId, today);
    }

    @Override
    public List<InquiryFaq> searchPublic(String keyword, LocalDate today, int limit) {
        return queryRepository.searchPublic(keyword, today, limit);
    }

    @Override
    public Map<Long, Long> countResolutionsByFaqIds(Collection<Long> faqIds) {
        return queryRepository.countResolutionsByFaqIds(faqIds);
    }

    @Override
    public List<FaqDemand> rankByDistinctAuthors(
        DemandTarget target, LocalDate today, Instant since, Long categoryId, int limit
    ) {
        return queryRepository.rankByDistinctAuthors(target, today, since, categoryId, limit);
    }

    @Override
    public InquiryFaq save(InquiryFaq faq) {
        return faqRepository.saveAndFlush(faq);
    }

    @Override
    public void saveResolution(Long faqId, Long memberId) {
        resolutionRepository.insertIfAbsent(faqId, memberId);
    }
}
