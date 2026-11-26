package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqAudienceInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffDetailInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryFaqStaffSummaryInfo;
import com.umc.product.inquiry.domain.enums.InquiryFaqStatus;

public interface GetInquiryFaqForStaffUseCase {

    List<InquiryFaqStaffSummaryInfo> listForStaff(Long staffMemberId, InquiryFaqStatus status, Long categoryId);

    InquiryFaqStaffDetailInfo getForStaff(Long staffMemberId, Long faqId);

    List<InquiryFaqAudienceInfo> listAudiences(Long staffMemberId);
}
