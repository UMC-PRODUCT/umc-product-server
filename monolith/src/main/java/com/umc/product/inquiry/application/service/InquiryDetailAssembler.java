package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryAuthorInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryCategoryInfo;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryDetailInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.enums.InquiryParticipant;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class InquiryDetailAssembler {

    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final GetMemberUseCase getMemberUseCase;
    private final InquiryAccessPolicy accessPolicy;

    InquiryDetailInfo assemble(Inquiry inquiry, Long requesterMemberId, InquiryParticipant requester) {
        InquiryCategoryInfo category = loadInquiryCategoryPort.findById(inquiry.getCategoryId())
            .map(InquiryCategoryInfo::from)
            .orElse(null);
        InquiryAuthorInfo author = getMemberUseCase.findById(inquiry.getAuthorMemberId())
            .map(member -> new InquiryAuthorInfo(
                member.id(), member.name(), member.nickname(), member.profileImageLink()
            ))
            .orElseGet(() -> new InquiryAuthorInfo(inquiry.getAuthorMemberId(), null, null, null));
        return new InquiryDetailInfo(
            inquiry.getId(),
            inquiry.getTitle(),
            category,
            inquiry.getTargetTeam(),
            inquiry.getStatus(),
            author,
            inquiry.getCreatedAt(),
            inquiry.getClosedAt(),
            inquiry.getLastMessageAt(),
            accessPolicy.canClose(inquiry, requesterMemberId, requester)
        );
    }
}
