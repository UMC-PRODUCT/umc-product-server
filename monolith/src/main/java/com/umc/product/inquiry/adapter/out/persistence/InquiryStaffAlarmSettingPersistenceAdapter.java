package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.inquiry.application.port.out.LoadInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.application.port.out.SaveInquiryStaffAlarmSettingPort;
import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryStaffAlarmSettingPersistenceAdapter
    implements LoadInquiryStaffAlarmSettingPort, SaveInquiryStaffAlarmSettingPort {

    private final InquiryStaffAlarmSettingJpaRepository repository;

    @Override
    public Optional<InquiryStaffAlarmSetting> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public List<Long> listDisabledMemberIds() {
        return repository.findAllDisabledMemberIds();
    }

    @Override
    public void upsert(Long memberId, boolean enabled) {
        repository.upsert(memberId, enabled);
    }
}
