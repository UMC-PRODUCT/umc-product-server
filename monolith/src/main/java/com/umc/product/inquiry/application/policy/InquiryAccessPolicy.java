package com.umc.product.inquiry.application.policy;

import org.springframework.stereotype.Component;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryAccessPolicy {

    private final CheckChallengerAuthorityUseCase checkChallengerAuthorityUseCase;

    public InquiryParticipant requireParticipant(Inquiry inquiry, Long memberId) {
        if (!canAccess(inquiry, memberId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
        }
        return inquiry.participantOf(memberId);
    }

    public boolean canAccess(Inquiry inquiry, Long memberId) {
        return inquiry.participantOf(memberId) == InquiryParticipant.AUTHOR || isStaff(memberId);
    }

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
