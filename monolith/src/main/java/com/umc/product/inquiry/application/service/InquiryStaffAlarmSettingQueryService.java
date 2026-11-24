package com.umc.product.inquiry.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.inquiry.application.policy.InquiryAccessPolicy;
import com.umc.product.inquiry.application.port.in.query.GetInquiryStaffAlarmSettingUseCase;
import com.umc.product.inquiry.application.port.in.query.ListInquiryStaffAlarmRecipientsUseCase;
import com.umc.product.inquiry.application.port.in.query.dto.InquiryStaffAlarmSettingInfo;
import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;
import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryStaffAlarmSettingQueryService
    implements GetInquiryStaffAlarmSettingUseCase, ListInquiryStaffAlarmRecipientsUseCase {

    private final LoadInquiryStaffAlarmSettingPort loadSettingPort;
    private final ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    private final InquiryAccessPolicy accessPolicy;

    @Override
    public InquiryStaffAlarmSettingInfo get(Long staffMemberId) {
        accessPolicy.requireStaff(staffMemberId);
        return loadSettingPort.findByMemberId(staffMemberId)
            .map(InquiryStaffAlarmSettingInfo::from)
            .orElseGet(InquiryStaffAlarmSettingInfo::defaultSetting);
    }

    @Override
    public List<Long> listRecipientMemberIds(Long senderMemberId) {
        Set<Long> excluded = new HashSet<>(loadSettingPort.listDisabledMemberIds());
        return listMemberSystemRoleUseCase.listUmcProductMemberIds().stream()
            .filter(memberId -> !excluded.contains(memberId))
            .filter(memberId -> !Objects.equals(memberId, senderMemberId))
            .toList();
    }
}
