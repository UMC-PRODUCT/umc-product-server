package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.inquiry.domain.InquiryLabel;

public interface InquiryLabelJpaRepository extends JpaRepository<InquiryLabel, Long> {

    List<InquiryLabel> findAllByOrderByNameAscIdAsc();
}
