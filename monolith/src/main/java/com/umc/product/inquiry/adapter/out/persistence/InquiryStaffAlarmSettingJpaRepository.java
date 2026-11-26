package com.umc.product.inquiry.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.inquiry.domain.InquiryStaffAlarmSetting;

public interface InquiryStaffAlarmSettingJpaRepository extends JpaRepository<InquiryStaffAlarmSetting, Long> {

    Optional<InquiryStaffAlarmSetting> findByMemberId(Long memberId);

    @Query("select setting.memberId from InquiryStaffAlarmSetting setting where setting.enabled = true")
    List<Long> findAllEnabledMemberIds();

    @Modifying
    @Query(value = """
        INSERT INTO inquiry_staff_alarm_setting (member_id, enabled, created_at, updated_at)
        VALUES (:memberId, :enabled, now(), now())
        ON CONFLICT (member_id) DO UPDATE SET enabled = EXCLUDED.enabled, updated_at = now()
        """, nativeQuery = true)
    void upsert(@Param("memberId") Long memberId, @Param("enabled") boolean enabled);
}
