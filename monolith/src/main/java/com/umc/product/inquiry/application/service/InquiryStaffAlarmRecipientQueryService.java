package com.umc.product.inquiry.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.chat.application.port.in.query.GetChatRoomUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryStaffAlarmRecipientsUseCase;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.domain.Inquiry;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStaffAlarmRecipientQueryService implements ListInquiryStaffAlarmRecipientsUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final LoadInquiryStaffAlarmSettingPort loadSettingPort;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final GetChatRoomUseCase getChatRoomUseCase;

    @Override
    public List<Long> listNewInquiryRecipientMemberIds(Long authorMemberId) {
        return staffExceptAuthor(staffIds(), Set.copyOf(loadSettingPort.listEnabledMemberIds()), authorMemberId);
    }

    @Override
    public List<Long> listAuthorMessageRecipientMemberIds(Long inquiryId) {
        return loadInquiryPort.findById(inquiryId)
            .map(this::authorMessageRecipients)
            .orElse(List.of());
    }

    private List<Long> authorMessageRecipients(Inquiry inquiry) {
        List<Long> staffIds = staffIds();
        Long authorId = inquiry.getAuthorMemberId();
        Long assigneeId = inquiry.effectiveAssigneeId(staffIds);
        if (assigneeId != null && !assigneeId.equals(authorId)) {
            return List.of(assigneeId);
        }
        List<Long> participants = staffExceptAuthor(staffIds, roomMembers(inquiry), authorId);
        if (!participants.isEmpty()) {
            return participants;
        }
        return staffExceptAuthor(staffIds, Set.copyOf(loadSettingPort.listEnabledMemberIds()), authorId);
    }

    // 운영진은 첫 메시지를 보낼 때만 문의 방에 들어가므로, 방 멤버가 곧 그 문의에 답한 사람이다.
    private Set<Long> roomMembers(Inquiry inquiry) {
        return Set.copyOf(getChatRoomUseCase.getById(inquiry.getChatRoomId(), inquiry.getAuthorMemberId()).memberIds());
    }

    private List<Long> staffIds() {
        return listMemberSystemRoleUseCase.listUmcProductMemberIds();
    }

    private static List<Long> staffExceptAuthor(List<Long> staffIds, Set<Long> candidates, Long authorMemberId) {
        return staffIds.stream()
            .filter(candidates::contains)
            .filter(memberId -> !Objects.equals(memberId, authorMemberId))
            .toList();
    }
}
