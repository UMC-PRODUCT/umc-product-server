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

    public InquiryAccess resolve(Inquiry inquiry, Long memberId) {
        boolean staff = isStaff(memberId);
        if (inquiry.isAuthor(memberId)) {
            return new InquiryAccess(InquiryParticipant.AUTHOR, staff);
        }
        if (staff) {
            return new InquiryAccess(InquiryParticipant.STAFF, true);
        }
        throw new InquiryDomainException(InquiryErrorCode.INQUIRY_ACCESS_DENIED);
    }

    public void requireStaff(Long memberId) {
        if (!isStaff(memberId)) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_STAFF_ONLY);
        }
    }

    private boolean isStaff(Long memberId) {
        return checkChallengerAuthorityUseCase.isUmcProduct(memberId);
    }

    /**
     * @param participant 대화에서 요청자의 자리. 운영진이 자기 문의를 보면 AUTHOR 다.
     * @param staff       요청자가 운영진인지. 종료 권한은 자리와 무관하게 이 값으로 정한다.
     */
    public record InquiryAccess(InquiryParticipant participant, boolean staff) {
    }
}
