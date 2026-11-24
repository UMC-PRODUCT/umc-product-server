package com.umc.product.inquiry.application.policy;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 문의는 작성자와 운영진(UMC_PRODUCT 또는 SUPER_ADMIN)만 읽고 쓸 수 있다.
 * <p>
 * 문의 채팅방은 Chat 엔진에서 공개 조회 범위(PUBLIC)라서, 방 조회 전에 반드시 이 정책을 거쳐야 한다.
 */
@Component
@RequiredArgsConstructor
public class InquiryAccessPolicy {

    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    /**
     * 요청자의 자리를 돌려준다. 작성자는 운영진 여부를 조회하지 않는다.
     */
    public InquiryParticipant requireParticipant(Inquiry inquiry, Long memberId) {
        InquiryParticipant participant = inquiry.participantOf(memberId);
        if (participant == InquiryParticipant.STAFF && !isStaff(memberId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
        }
        return participant;
    }

    /**
     * @param participant {@link #requireParticipant}가 돌려준 자리. STAFF 면 이미 운영진임을 확인했다.
     */
    public boolean canClose(Inquiry inquiry, Long memberId, InquiryParticipant participant) {
        return inquiry.isClosable() && (participant == InquiryParticipant.STAFF || isStaff(memberId));
    }

    public void requireStaff(Long memberId) {
        if (!isStaff(memberId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_STAFF_ONLY);
        }
    }

    private boolean isStaff(Long memberId) {
        return checkChallengerAuthorityUseCase.isUmcProduct(memberId);
    }
}
