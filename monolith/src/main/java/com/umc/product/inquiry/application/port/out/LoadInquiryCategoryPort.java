package com.umc.product.inquiry.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.umc.product.inquiry.domain.InquiryCategory;

public interface LoadInquiryCategoryPort {

    Optional<InquiryCategory> findById(Long categoryId);

    List<InquiryCategory> listActive();

    /** 비활성 카테고리도 포함한다. */
    List<InquiryCategory> listByIds(Collection<Long> categoryIds);
}
