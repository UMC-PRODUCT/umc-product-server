package com.umc.product.inquiry.domain.enums;

/**
 * 문의 대화에서 요청자가 맡는 자리.
 * <p>
 * 운영진이 자기 문의를 남긴 경우에는 작성자({@code AUTHOR})로 본다.
 */
public enum InquiryParticipant {
    AUTHOR,
    STAFF
}
