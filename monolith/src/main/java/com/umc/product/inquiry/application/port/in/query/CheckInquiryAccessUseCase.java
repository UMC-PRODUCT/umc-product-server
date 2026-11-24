package com.umc.product.inquiry.application.port.in.query;

public interface CheckInquiryAccessUseCase {

    /**
     * 작성자 또는 운영진이면 true. 없는 문의는 false이며 예외를 던지지 않는다.
     */
    boolean canAccess(Long memberId, Long inquiryId);
}
