package com.umc.product.inquiry.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.inquiry.application.event.InquiryAssignedEvent;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.policy.InquiryStaffRoster;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryAssigneeUseCase;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryTeamUseCase;
import com.umc.product.inquiry.application.port.in.command.LinkInquiryFaqUseCase;
import com.umc.product.inquiry.application.port.in.command.ReplaceInquiryLabelsUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryAssigneeCommand;
import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryTeamCommand;
import com.umc.product.inquiry.application.port.in.command.dto.LinkInquiryFaqCommand;
import com.umc.product.inquiry.application.port.in.command.dto.ReplaceInquiryLabelsCommand;
import com.umc.product.inquiry.application.port.out.LoadInquiryCategoryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryFaqPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryHistoryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.InquiryCategory;
import com.umc.product.inquiry.domain.InquiryFaq;
import com.umc.product.inquiry.domain.InquiryFaqContent;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;

import lombok.RequiredArgsConstructor;

/**
 * 운영진이 문의를 처리하며 바꾸는 담당자·받는 팀·라벨·대표 질문 연결. 모두 문의 행을 잠근 뒤 바꾼다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryManagementCommandService implements
    ChangeInquiryAssigneeUseCase,
    ChangeInquiryTeamUseCase,
    ReplaceInquiryLabelsUseCase,
    LinkInquiryFaqUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    private final SaveInquiryHistoryPort saveInquiryHistoryPort;
    private final LoadInquiryLabelPort loadInquiryLabelPort;
    private final SaveInquiryLabelPort saveInquiryLabelPort;
    private final LoadInquiryFaqPort loadInquiryFaqPort;
    private final SaveInquiryFaqPort saveInquiryFaqPort;
    private final LoadInquiryCategoryPort loadInquiryCategoryPort;
    private final InquiryStaffRoster staffRoster;
    private final InquiryAccessPolicy accessPolicy;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public void changeAssignee(ChangeInquiryAssigneeCommand command) {
        Inquiry inquiry = loadLockedForStaff(command.inquiryId(), command.actorMemberId());
        inquiry.changeAssignee(
            command.actorMemberId(),
            command.assigneeMemberId(),
            command.expectedAssigneeMemberId(),
            staffRoster.memberIds()
        ).ifPresent(history -> {
            saveInquiryHistoryPort.save(history);
            if (history.notifiesNewAssignee()) {
                domainEventPublisher.publish(InquiryAssignedEvent.of(
                    inquiry.getId(), history.getNewAssigneeMemberId(), command.actorMemberId(), inquiry.getTitle()
                ));
            }
        });
        saveInquiryPort.save(inquiry);
    }

    @Override
    public void changeTeam(ChangeInquiryTeamCommand command) {
        Inquiry inquiry = loadLockedForStaff(command.inquiryId(), command.actorMemberId());
        inquiry.changeTeam(command.actorMemberId(), command.targetTeam()).ifPresent(saveInquiryHistoryPort::save);
        saveInquiryPort.save(inquiry);
    }

    @Override
    public void replaceLabels(ReplaceInquiryLabelsCommand command) {
        Inquiry inquiry = loadLockedForStaff(command.inquiryId(), command.actorMemberId());
        if (loadInquiryLabelPort.listByIds(command.labelIds()).size() != command.labelIds().size()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_LABEL_NOT_FOUND);
        }
        saveInquiryLabelPort.replaceAssignments(inquiry.getId(), command.labelIds());
    }

    @Override
    public void linkFaq(LinkInquiryFaqCommand command) {
        Inquiry inquiry = loadLockedForStaff(command.inquiryId(), command.actorMemberId());
        if (command.faqId() != null && loadInquiryFaqPort.findById(command.faqId()).isEmpty()) {
            throw new InquiryDomainException(InquiryErrorCode.INQUIRY_FAQ_NOT_FOUND);
        }
        inquiry.linkFaq(command.faqId());
        saveInquiryPort.save(inquiry);
    }

    @Override
    public Long linkNewDraftFaq(Long inquiryId, Long actorMemberId) {
        Inquiry inquiry = loadLockedForStaff(inquiryId, actorMemberId);
        InquiryFaq faq = saveInquiryFaqPort.save(InquiryFaq.create(
            InquiryFaqContent.draftOf(inquiry.getTitle(), activeCategoryIdOf(inquiry)), actorMemberId
        ));
        inquiry.linkFaq(faq.getId());
        saveInquiryPort.save(inquiry);
        return faq.getId();
    }

    // 지금은 쓰지 않는 옛 분류로 접수된 문의면 초안에 분류를 넣지 않는다. 발행 전에 운영진이 고른다.
    private Long activeCategoryIdOf(Inquiry inquiry) {
        return loadInquiryCategoryPort.findById(inquiry.getCategoryId())
            .filter(InquiryCategory::isActive)
            .map(InquiryCategory::getId)
            .orElse(null);
    }

    private Inquiry loadLockedForStaff(Long inquiryId, Long actorMemberId) {
        accessPolicy.requireStaff(actorMemberId);
        return loadInquiryPort.findByIdForUpdate(inquiryId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }
}
