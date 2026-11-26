package com.umc.product.inquiry.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.inquiry.domain.InquiryFaqResolution;

public interface InquiryFaqResolutionJpaRepository extends JpaRepository<InquiryFaqResolution, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO inquiry_faq_resolution (faq_id, member_id, created_at, updated_at)
        VALUES (:faqId, :memberId, now(), now())
        ON CONFLICT (faq_id, member_id) DO NOTHING
        """, nativeQuery = true)
    void insertIfAbsent(@Param("faqId") Long faqId, @Param("memberId") Long memberId);
}
