package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.inquiry.domain.InquiryCategory;

public interface InquiryCategoryJpaRepository extends JpaRepository<InquiryCategory, Long> {

    List<InquiryCategory> findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
}
