package com.umc.product.inquiry.application.port.out;

import com.umc.product.inquiry.domain.InquiryFaq;

public interface SaveInquiryFaqPort {

    InquiryFaq save(InquiryFaq faq);

    /** 같은 회원이 다시 눌러도 한 번만 남는다. */
    void saveResolution(Long faqId, Long memberId);
}
