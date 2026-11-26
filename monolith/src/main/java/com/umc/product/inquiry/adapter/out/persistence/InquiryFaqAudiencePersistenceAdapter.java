package com.umc.product.inquiry.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.out.LoadInquiryFaqAudiencePort;
import com.umc.product.inquiry.domain.InquiryFaqAudience;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryFaqAudiencePersistenceAdapter implements LoadInquiryFaqAudiencePort {

    private final InquiryFaqAudienceJpaRepository audienceRepository;

    @Override
    public List<InquiryFaqAudience> listActive() {
        return audienceRepository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
    }

    @Override
    public Optional<InquiryFaqAudience> findById(Long audienceId) {
        return audienceRepository.findById(audienceId);
    }

    @Override
    public List<InquiryFaqAudience> listByIds(Collection<Long> audienceIds) {
        return audienceIds.isEmpty() ? List.of() : audienceRepository.findAllById(audienceIds);
    }
}
