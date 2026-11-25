package com.umc.product.inquiry.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.chat.application.port.in.query.GetChatRoomUseCase;
import com.umc.product.inquiry.application.policy.InquiryStaffRoster;
import com.umc.product.inquiry.application.port.in.query.ListInquiryStaffAlarmRecipientsUseCase;
import com.umc.product.inquiry.application.port.out.LoadInquiryPort;
import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.domain.Inquiry;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStaffAlarmRecipientQueryService implements ListInquiryStaffAlarmRecipientsUseCase {

    private final LoadInquiryPort loadInquiryPort;
    private final LoadInquiryStaffAlarmSettingPort loadSettingPort;
    private final InquiryStaffRoster staffRoster;
    private final GetChatRoomUseCase getChatRoomUseCase;

    @Override
    public List<Long> listNewInquiryRecipientMemberIds(Long authorMemberId) {
        Set<Long> enabled = Set.copyOf(loadSettingPort.listEnabledMemberIds());
        return staffExceptAuthor(staffRoster.memberIds(), enabled, authorMemberId);
    }

    @Override
    public List<Long> listAuthorMessageRecipientMemberIds(Long inquiryId) {
        return loadInquiryPort.findById(inquiryId)
            .map(this::authorMessageRecipients)
            .orElse(List.of());
    }

    private List<Long> authorMessageRecipients(Inquiry inquiry) {
        Set<Long> staffIds = staffRoster.memberIds();
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

    private static List<Long> staffExceptAuthor(Set<Long> staffIds, Set<Long> candidates, Long authorMemberId) {
        return staffIds.stream()
            .filter(candidates::contains)
            .filter(memberId -> !Objects.equals(memberId, authorMemberId))
            .toList();
    }
}
