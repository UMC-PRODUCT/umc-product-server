package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

public interface InquiryStaffAlarmSettingJpaRepository extends JpaRepository<InquiryStaffAlarmSetting, Long> {

    Optional<InquiryStaffAlarmSetting> findByMemberId(Long memberId);

    @Query("select setting.memberId from InquiryStaffAlarmSetting setting where setting.enabled = false")
    List<Long> findAllDisabledMemberIds();
}
