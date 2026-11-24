package com.umc.product.inquiry.domain;

import com.umc.product.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 운영진 한 명의 문의 알림 설정. 행이 없으면 켜짐으로 본다.
 */
@Entity
@Table(
    name = "inquiry_staff_alarm_setting",
    uniqueConstraints = @UniqueConstraint(name = "uq_inquiry_staff_alarm_setting_member", columnNames = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryStaffAlarmSetting extends BaseEntity {

    public static final boolean DEFAULT_ENABLED = true;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    private InquiryStaffAlarmSetting(Long memberId, boolean enabled) {
        this.memberId = memberId;
        this.enabled = enabled;
    }

    public static InquiryStaffAlarmSetting create(Long memberId, boolean enabled) {
        return new InquiryStaffAlarmSetting(memberId, enabled);
    }

    public void changeEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
