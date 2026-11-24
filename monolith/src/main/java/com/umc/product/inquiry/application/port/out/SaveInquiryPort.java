package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.domain.Inquiry;

public interface SaveInquiryPort {

    /**
     * 새 문의를 저장하고 즉시 flush 한다. 같은 작성자의 같은 등록 요청이 동시에 들어와 unique 제약에 걸리면
     * {@code INQUIRY_REQUEST_CONFLICT}로 바꿔 던진다.
     */
    Inquiry create(Inquiry inquiry);

    Inquiry save(Inquiry inquiry);
}
