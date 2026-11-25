package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.ManageInquiryLabelUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.SaveInquiryLabelCommand;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryLabelInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryLabelPort;
import com.umc.product.inquiry.domain.InquiryLabel;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryLabelCommandService implements ManageInquiryLabelUseCase {

    private final LoadInquiryLabelPort loadInquiryLabelPort;
    private final SaveInquiryLabelPort saveInquiryLabelPort;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryLabelInfo create(SaveInquiryLabelCommand command) {
        accessPolicy.requireStaff(command.actorMemberId());
        return InquiryLabelInfo.from(saveInquiryLabelPort.save(InquiryLabel.create(command.name())));
    }

    @Override
    public InquiryLabelInfo rename(SaveInquiryLabelCommand command) {
        InquiryLabel label = loadForStaff(command.actorMemberId(), command.labelId());
        label.rename(command.name());
        return InquiryLabelInfo.from(saveInquiryLabelPort.save(label));
    }

    @Override
    public void delete(Long actorMemberId, Long labelId) {
        saveInquiryLabelPort.delete(loadForStaff(actorMemberId, labelId));
    }

    private InquiryLabel loadForStaff(Long actorMemberId, Long labelId) {
        accessPolicy.requireStaff(actorMemberId);
        return loadInquiryLabelPort.findById(labelId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_LABEL_NOT_FOUND));
    }
}
