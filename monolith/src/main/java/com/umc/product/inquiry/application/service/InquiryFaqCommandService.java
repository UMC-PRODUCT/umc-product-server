package com.umc.product.inquiry.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.ManageInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.command.MarkInquiryFaqResolvedUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.ArchiveInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.CreateInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.SaveInquiryFaqDraftCommand;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqAudiencePort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryFaqPort;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqAudience;
import com.umc.product.inquiry.domain.InquiryFaqContent;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryFaqCommandService implements ManageInquiryFaqUseCase, MarkInquiryFaqResolvedUseCase {

    private final LoadInquiryFaqPort loadInquiryFaqPort;
    private final SaveInquiryFaqPort saveInquiryFaqPort;
    private final LoadInquiryFaqAudiencePort loadInquiryFaqAudiencePort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final InquiryAccessPolicy accessPolicy;
    private final Clock clock;

    @Override
    public Long create(CreateInquiryFaqCommand command) {
        accessPolicy.requireStaff(command.actorMemberId());
        InquiryFaqContent content = validated(command.content().toContent());
        return saveInquiryFaqPort.save(InquiryFaq.create(content, command.actorMemberId())).getId();
    }

    @Override
    public void saveDraft(SaveInquiryFaqDraftCommand command) {
        InquiryFaq faq = loadForStaff(command.actorMemberId(), command.faqId(), command.expectedVersion());
        faq.saveDraft(validated(command.content().toContent()));
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void publish(PublishInquiryFaqCommand command) {
        InquiryFaq faq = loadForStaff(command.actorMemberId(), command.faqId(), command.expectedVersion());
        validated(faq.getDraft());
        faq.publish(command.actorMemberId(), command.verified(), clock.instant());
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void archive(ArchiveInquiryFaqCommand command) {
        InquiryFaq faq = loadForStaff(command.actorMemberId(), command.faqId(), command.expectedVersion());
        faq.archive();
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void markResolved(Long memberId, Long faqId) {
        loadInquiryFaqPort.findById(faqId)
            .filter(faq -> faq.isPublicAt(clock.instant()))
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND));
        saveInquiryFaqPort.saveResolution(faqId, memberId);
    }

    private InquiryFaqContent validated(InquiryFaqContent content) {
        if (content.getCategoryId() != null && loadInquiryCategoryPort.findById(content.getCategoryId())
            .filter(InquiryCategory::isActive).isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_CATEGORY_NOT_FOUND);
        }
        if (content.getAudienceId() != null && loadInquiryFaqAudiencePort.findById(content.getAudienceId())
            .filter(InquiryFaqAudience::isActive).isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_AUDIENCE_NOT_FOUND);
        }
        if (content.getRelatedFaqId() != null && loadInquiryFaqPort.findById(content.getRelatedFaqId()).isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND);
        }
        return content;
    }

    private InquiryFaq loadForStaff(Long actorMemberId, Long faqId, long expectedVersion) {
        accessPolicy.requireStaff(actorMemberId);
        InquiryFaq faq = loadInquiryFaqPort.findById(faqId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND));
        faq.requireVersion(expectedVersion);
        return faq;
    }
}
