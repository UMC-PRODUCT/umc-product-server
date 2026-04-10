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

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location; // 출석을 요청한 위치

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // 출석 상태

    private Long decidedMemberId; // 출석 요청을 승인 또는 기각한 사람

    private Instant decidedAt;

    private boolean isLocationVerified; // 클라이언트 단 판단으로, 위치 인증 여부

    private String excuseReason;

    // 단순 생성자로 사용할 것, 실제 생성 로직은 ScheduleParticipant에 있음
    @Builder
    private ScheduleParticipantAttendance(
        Point location,
        AttendanceStatus status,
        Long decidedMemberId,
        Instant decidedAt,
        boolean isLocationVerified,
        String excuseReason
    ) {
        this.location = location;
        this.status = status;
        this.decidedMemberId = decidedMemberId;
        this.decidedAt = decidedAt;
        this.isLocationVerified = isLocationVerified;
        this.excuseReason = excuseReason;
    }

    // Service Layer에서 아래 메소드들을 직접적으로 사용할 수 없도록, protected를 사용합니다.
    // ScheduleParticipant 내부에서 래핑하여 사용됩니다.

    protected void approve(Long approvedByMemberId) {
        this.decidedMemberId = approvedByMemberId;
        this.decidedAt = Instant.now();

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

    protected void reject(Long rejectedByMemberId) {
        this.decidedMemberId = rejectedByMemberId;
        this.decidedAt = Instant.now();

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
    protected void forceChange(Long decidedMemberId, AttendanceStatus status) {
        this.decidedMemberId = decidedMemberId;
        this.decidedAt = Instant.now();

        this.status = status;
    }
}
