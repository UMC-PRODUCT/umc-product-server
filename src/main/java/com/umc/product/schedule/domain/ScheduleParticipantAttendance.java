package com.umc.product.schedule.domain;

import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

/**
 * 사용자가 출석 요청을 했을 때, 관련 정보가 담기는 Embeddable 객체.
 * <p>
 * 출석 요청 전에는 null로 유지됩니다.
 * <p>
 * see also {@link AttendanceStatus}
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipantAttendance {

    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    private Point location; // 출석을 요청한 위치

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status")
    private AttendanceStatus status; // 출석 상태

    @Column(name = "decided_by_member_id")
    private Long decidedByMemberId; // 출석 요청을 승인 또는 기각한 사람

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_reason", length = 300)
    private String decisionReason; // 출석 요청을 결정한 사유

    @Column(name = "is_location_verified")
    private Boolean locationVerified; // 클라이언트 단 판단으로, 위치 인증 여부

    @Column(name = "excuse_reason", length = 300)
    private String excuseReason;

    // 단순 생성자로 사용할 것, 실제 생성 로직은 ScheduleParticipant에 있음
    @Builder(access = AccessLevel.PRIVATE)
    private ScheduleParticipantAttendance(
        Point location,
        AttendanceStatus status,
        Long decidedByMemberId,
        Instant decidedAt,
        String decisionReason,
        boolean locationVerified,
        String excuseReason
    ) {
        this.location = location;
        this.status = status;
        this.decidedByMemberId = decidedByMemberId;
        this.decidedAt = decidedAt;
        this.decisionReason = decisionReason;
        this.locationVerified = locationVerified;
        this.excuseReason = excuseReason;
    }

    // 출석 요청 시 ScheduleParticipant에서 호출되어 사용됨.
    public static ScheduleParticipantAttendance create(
        Point location,
        boolean isLocationVerified,
        String excuseReason,
        AttendanceStatus status
    ) {
        return ScheduleParticipantAttendance.builder()
            .location(location)
            .locationVerified(isLocationVerified)
            .excuseReason(excuseReason)
            .status(status)
            .build();
    }

    // Service Layer에서 아래 메소드들을 직접적으로 사용할 수 없도록, protected를 사용합니다.
    // ScheduleParticipant 내부에서 래핑하여 사용됩니다.

    protected void approve(Long approvedByMemberId, String reason) {
        this.decidedByMemberId = approvedByMemberId;
        this.decidedAt = Instant.now();
        this.decisionReason = reason;

        this.status = switch (this.status) {
            case PRESENT_PENDING -> AttendanceStatus.PRESENT;
            case LATE_PENDING -> AttendanceStatus.LATE;
            case EXCUSED_PENDING,
                 ABSENT_EXCUSE_PENDING,
                 LATE_EXCUSE_PENDING -> AttendanceStatus.EXCUSED;

            default -> throw new ScheduleDomainException(
                ScheduleErrorCode.INVALID_ATTENDANCE_STATUS_FOR_APPROVAL
                , this.status + " 상태에서는 출석 요청을 승인할 수 없습니다."
            );
        };
    }

    protected void reject(Long rejectedByMemberId, String reason) {
        this.decidedByMemberId = rejectedByMemberId;
        this.decidedAt = Instant.now();
        this.decisionReason = reason;

        this.status = switch (this.status) {
            case PRESENT_PENDING,
                 LATE_PENDING,
                 EXCUSED_PENDING,
                 ABSENT_EXCUSE_PENDING -> AttendanceStatus.ABSENT;

            case LATE_EXCUSE_PENDING -> AttendanceStatus.LATE;

            default -> throw new ScheduleDomainException(
                ScheduleErrorCode.INVALID_ATTENDANCE_STATUS_FOR_REJECT,
                this.status + " 상태에서는 출석 요청을 기각할 수 없습니다."
            );
        };
    }

    /**
     * 매뉴얼한 운영진의 변경입니다. 일반적인 승인/기각이 아닌 사후에 상태를 변경하고자 할 때 사용합니다.
     */
    protected void forceChange(Long decidedByMemberId, AttendanceStatus status) {
        this.decidedByMemberId = decidedByMemberId;
        this.decidedAt = Instant.now();

        this.status = status;
    }
}
