package com.umc.product.inquiry.application.service;

import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.global.event.application.port.out.DomainEventPublisher;
import com.umc.product.inquiry.application.event.InquiryAssignedEvent;
import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryAssigneeUseCase;
import com.umc.product.inquiry.application.port.in.command.ChangeInquiryTeamUseCase;
import com.umc.product.inquiry.application.port.in.command.ReplaceInquiryLabelsUseCase;
import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryAssigneeCommand;
import com.umc.product.inquiry.application.port.in.command.dto.ChangeInquiryTeamCommand;
import com.umc.product.inquiry.application.port.in.command.dto.ReplaceInquiryLabelsCommand;
import com.umc.product.inquiry.application.port.out.LoadInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryHistoryPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryLabelPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.inquiry.domain.exception.InquiryDomainException;
import com.umc.product.inquiry.domain.exception.InquiryErrorCode;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

/**
 * 운영진이 문의를 처리하며 바꾸는 담당자·받는 팀·라벨. 모두 문의 행을 잠근 뒤 바꾼다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InquiryManagementCommandService implements
    ChangeInquiryAssigneeUseCase,
    ChangeInquiryTeamUseCase,
    ReplaceInquiryLabelsUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final SaveInquiryPort saveInquiryPort;
    private final SaveInquiryHistoryPort saveInquiryHistoryPort;
    private final LoadInquiryLabelPort loadInquiryLabelPort;
    private final SaveInquiryLabelPort saveInquiryLabelPort;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final InquiryAccessPolicy accessPolicy;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    public void changeAssignee(ChangeInquiryAssigneeCommand command) {
        Inquiry inquiry = loadLockedForStaff(command.inquiryId(), command.actorMemberId());
        Set<Long> staffIds = Set.copyOf(listMemberSystemRoleUseCase.listUmcProductMemberIds());
        inquiry.changeAssignee(
            command.actorMemberId(), command.assigneeMemberId(), command.expectedAssigneeMemberId(), staffIds
        ).ifPresent(history -> {
            saveInquiryHistoryPort.save(history);
            if (history.getNewAssigneeMemberId() != null
                && !Objects.equals(history.getNewAssigneeMemberId(), command.actorMemberId())) {
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

    private Inquiry loadLockedForStaff(Long inquiryId, Long actorMemberId) {
        accessPolicy.requireStaff(actorMemberId);
        return loadInquiryPort.findByIdForUpdate(inquiryId)
            .orElseThrow(() -> new InquiryDomainException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }
}
