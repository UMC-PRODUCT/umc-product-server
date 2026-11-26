package com.umc.product.inquiry.application.port.in.query;

import java.util.List;

import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;

public interface ListInquiryLabelsUseCase {

    List<InquiryLabelInfo> listLabels(Long staffMemberId);
}
