package com.umc.product.inquiry.application.port.out;

import java.util.Collection;

import com.umc.product.inquiry.domain.InquiryLabel;

public interface SaveInquiryLabelPort {

    /**
     * 저장하고 즉시 flush 한다. 이름이 겹쳐 unique 제약에 걸리면 {@code INQUIRY_LABEL_NAME_DUPLICATED}로 바꿔 던진다.
     */
    InquiryLabel save(InquiryLabel label);

    /** 붙어 있던 문의에서도 함께 빠진다. */
    void delete(InquiryLabel label);

    /** 문의의 라벨을 주어진 집합으로 통째로 바꾼다. */
    void replaceAssignments(Long inquiryId, Collection<Long> labelIds);
}
