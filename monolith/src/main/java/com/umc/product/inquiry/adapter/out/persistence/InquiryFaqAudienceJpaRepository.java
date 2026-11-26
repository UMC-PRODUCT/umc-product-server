package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.inquiry.domain.InquiryFaqAudience;

public interface InquiryFaqAudienceJpaRepository extends JpaRepository<InquiryFaqAudience, Long> {

    List<InquiryFaqAudience> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
}
