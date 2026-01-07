package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "schedule_attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleAttendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    private Long confirmedChallengerId;

    private String reason;

    @Builder
    private ScheduleAttendance(Long memberId, Schedule schedule, Point location, AttendanceStatus status) {
        this.memberId = memberId;
        this.schedule = schedule;
        this.location = location;
        this.status = status != null ? status : AttendanceStatus.PENDING;
    }

    public void approve(Long confirmerId) {
        this.confirmedChallengerId = confirmerId;
        this.status = resolveApprovedStatus();
    }

    public void reject(Long confirmerId) {
        this.confirmedChallengerId = confirmerId;
        this.status = AttendanceStatus.ABSENT;
    }

    private AttendanceStatus resolveApprovedStatus() {
        return switch (this.status) {
            case PRESENT_PENDING -> AttendanceStatus.PRESENT;
            case LATE_PENDING -> AttendanceStatus.LATE;
            case EXCUSED_PENDING -> AttendanceStatus.EXCUSED;
            default -> this.status;
        };
    }

    public boolean isPending() {
        return this.status == AttendanceStatus.PRESENT_PENDING
                || this.status == AttendanceStatus.LATE_PENDING
                || this.status == AttendanceStatus.EXCUSED_PENDING;
    }
}
