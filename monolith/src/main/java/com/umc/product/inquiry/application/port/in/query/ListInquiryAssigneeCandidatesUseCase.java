package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffMemberInfo;

public interface ListInquiryAssigneeCandidatesUseCase {

    /** {@code UMC_PRODUCT} 역할을 직접 가진 회원 전원을 이름순으로 돌려준다. */
    List<InquiryStaffMemberInfo> listCandidates(Long staffMemberId);
}
