package com.umc.product.inquiry.application.port.out;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.umc.product.inquiry.domain.InquiryLabel;

public interface LoadInquiryLabelPort {

    /** 이름순으로 돌려준다. */
    List<InquiryLabel> listAll();

    Optional<InquiryLabel> findById(Long labelId);

    List<InquiryLabel> listByIds(Collection<Long> labelIds);

    /** 문의마다 붙은 라벨을 이름순으로 돌려준다. 라벨이 없는 문의는 키가 없다. */
    Map<Long, List<InquiryLabel>> listByInquiryIds(Collection<Long> inquiryIds);
}
