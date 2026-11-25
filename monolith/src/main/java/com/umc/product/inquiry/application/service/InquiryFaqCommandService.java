package com.umc.product.inquiry.application.service;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.LinkInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.command.ManageInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.command.MarkInquiryFaqResolvedUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.InquiryFaqContentCommand;
import com.umc.product.inquiry.application.port.in.command.dto.LinkInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.PublishInquiryFaqCommand;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqAudiencePort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqAudience;
import com.umc.product.inquiry.domain.InquiryFaqContent;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 대표 질문 작성·발행·보관과 문의 연결. 운영진(AUTHZ-10) 누구나 할 수 있고 별도 발행 권한은 없다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryFaqCommandService implements
    ManageInquiryFaqUseCase,
    LinkInquiryFaqUseCase,
    MarkInquiryFaqResolvedUseCase {

    private final LoadInquiryFaqPort loadInquiryFaqPort;
    private final SaveInquiryFaqPort saveInquiryFaqPort;
    private final LoadInquiryFaqAudiencePort loadInquiryFaqAudiencePort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final LoadInquiryPort loadInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    private final InquiryAccessPolicy accessPolicy;
    private final Clock clock;

    @Override
    public Long create(Long actorMemberId, InquiryFaqContentCommand content) {
        accessPolicy.requireStaff(actorMemberId);
        return saveInquiryFaqPort.save(InquiryFaq.create(validated(content.toContent()), actorMemberId)).getId();
    }

    @Override
    public void saveDraft(Long actorMemberId, Long faqId, InquiryFaqContentCommand content) {
        InquiryFaq faq = loadForStaff(actorMemberId, faqId);
        faq.saveDraft(validated(content.toContent()));
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void publish(PublishInquiryFaqCommand command) {
        InquiryFaq faq = loadForStaff(command.actorMemberId(), command.faqId());
        faq.publish(command.actorMemberId(), command.verified(), clock.instant());
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void archive(Long actorMemberId, Long faqId) {
        InquiryFaq faq = loadForStaff(actorMemberId, faqId);
        faq.archive();
        saveInquiryFaqPort.save(faq);
    }

    @Override
    public void linkFaq(LinkInquiryFaqCommand command) {
        accessPolicy.requireStaff(command.actorMemberId());
        if (command.faqId() != null && loadInquiryFaqPort.findById(command.faqId()).isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND);
        }
        Inquiry inquiry = loadLockedInquiry(command.inquiryId());
        inquiry.linkFaq(command.faqId());
        saveInquiryPort.save(inquiry);
    }

    @Override
    public Long linkNewDraftFaq(Long inquiryId, Long actorMemberId) {
        accessPolicy.requireStaff(actorMemberId);
        Inquiry inquiry = loadLockedInquiry(inquiryId);
        InquiryFaq faq = saveInquiryFaqPort.save(InquiryFaq.create(
            InquiryFaqContent.draftOf(inquiry.getTitle(), inquiry.getCategoryId()), actorMemberId
        ));
        inquiry.linkFaq(faq.getId());
        saveInquiryPort.save(inquiry);
        return faq.getId();
    }

    @Override
    public void markResolved(Long memberId, Long faqId) {
        loadInquiryFaqPort.findById(faqId)
            .filter(faq -> faq.isPublicOn(InquiryFaq.today(clock.instant())))
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

    private InquiryFaq loadForStaff(Long actorMemberId, Long faqId) {
        accessPolicy.requireStaff(actorMemberId);
        return loadInquiryFaqPort.findById(faqId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND));
    }

    private Inquiry loadLockedInquiry(Long inquiryId) {
        return loadInquiryPort.findByIdForUpdate(inquiryId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }
}
