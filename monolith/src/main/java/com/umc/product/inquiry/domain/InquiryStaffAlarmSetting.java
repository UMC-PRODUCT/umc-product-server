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
 * 운영진 한 명의 새 문의 알림 설정. 행이 없으면 꺼짐으로 본다. 50명 가까운 운영진 모두에게 새 문의가 가지 않게
 * 받고 싶은 사람만 켠다.
 */
@Entity
@Table(
    name = "inquiry_staff_alarm_setting",
    uniqueConstraints = @UniqueConstraint(name = "uq_inquiry_staff_alarm_setting_member", columnNames = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryStaffAlarmSetting extends BaseEntity {

    public static final boolean DEFAULT_ENABLED = false;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;
}
