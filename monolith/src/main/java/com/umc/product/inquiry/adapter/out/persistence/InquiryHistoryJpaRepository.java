package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.inquiry.domain.InquiryHistory;

public interface InquiryHistoryJpaRepository extends JpaRepository<InquiryHistory, Long> {

    List<InquiryHistory> findAllByInquiryIdOrderByIdAsc(Long inquiryId);
}
