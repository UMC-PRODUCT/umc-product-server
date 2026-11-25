package com.umc.product.inquiry.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.umc.product.inquiry.domain.InquiryFaqAudience;

public interface LoadInquiryFaqAudiencePort {

    List<InquiryFaqAudience> listActive();

    Optional<InquiryFaqAudience> findById(Long audienceId);

    /** 비활성 대상도 포함한다. */
    List<InquiryFaqAudience> listByIds(Collection<Long> audienceIds);
}
