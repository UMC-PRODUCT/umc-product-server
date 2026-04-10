package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
@Table(name = "schedule_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    // 우선은 LAZY로 두고, 추후 fetch join 등을 사용하여 필요시 최적화
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule; // 단방향 참조

    // 출석 관련 정보를 모아두었음
    @Embedded
    private ScheduleParticipantAttendance attendance;

    @Builder
    private ScheduleParticipant(
        Long memberId,
        Schedule schedule,
        ScheduleParticipantAttendance attendance
    ) {
        this.memberId = memberId;
        this.schedule = schedule;
        this.attendance = attendance;
    }

    // TODO: 비대면 일정일 경우를 고려해야함 -> 일정의 location이 비어있는 경우로 간주
    // 해당 경우는 시간에 따라서 요청을 보낼 수 있도록 해야함 .. 근데 모두 다 서비스의 책임이긴 함

    // Attendance가 생성되는 시점:
    // - 사유 출석
    // - 일반적인 요청
    // - 운영진이 직접 업데이트 하는 경우
    // - (미정) 스케쥴러가 변경하는 상황
    public void createAttendance(
        Point location,
        boolean isLocationVerified
    ) {
        // 이미 출석 요청을 한 기록이 있다면 update 하는 method를 사용해야함
        if (this.attendance != null) {
            throw new ScheduleDomainException(ScheduleErrorCode.NOT_FIRST_ATTENDANCE_REQUEST);
        }

        this.attendance =
            ScheduleParticipantAttendance.builder()
                .location(location)
                .status(this.schedule.getAttendanceStatus())
                .isLocationVerified(isLocationVerified)
                .build();
    }

    // 사유제출하는 경우
    // 최초 요청인 경우: EXCUSED_PENDING으로 전환할 것
    // LATE, ABSENT인 경우: 각각 상태로 전이시킬 것

    /**
     * 사유를 제출하는 경우를 핸들링합니다.
     * <p>
     * - 최초 요청인 경우: EXCUSED_PENDING으로 상태가 설정됩니다.
     * <p>
     * - 이미 출석 요청이 존재하는 경우: 기존 상태가 지각/결석 일 때만 가능하며, 각각 상태에 따라서 전이됩니다.
     *
     * @param location           필요 시 제공합니다.
     * @param isLocationVerified 클라이언트 측 위치 인증 여부입니다. 필요 시 제공합니다.
     * @param excuseReason       제출된 사유입니다. 필수로 입력하셔야 합니다.
     */
    public void submitExcuse(
        Point location,
        boolean isLocationVerified,
        String excuseReason
    ) {
        if (excuseReason == null || excuseReason.isEmpty()) {
            throw new ScheduleDomainException(ScheduleErrorCode.NO_EXCUSE_REASON_GIVEN);
        }
        // 기존에 출석 요청이 존재하지 않는 경우
        if (this.attendance == null) {
            this.attendance =
                ScheduleParticipantAttendance.builder()
                    .location(location)
                    .status(AttendanceStatus.EXCUSED_PENDING)
                    .isLocationVerified(isLocationVerified)
                    .excuseReason(excuseReason)
                    .build();
        }

        // 기존에 출석 요청이 존재하는 경우
        else {
            // 기존에 결석이였던 경우
            if (this.attendance.getStatus() == AttendanceStatus.ABSENT) {
                this.attendance = ScheduleParticipantAttendance.builder()
                    .location(location)
                    .isLocationVerified(isLocationVerified)
                    .status(AttendanceStatus.ABSENT_EXCUSE_PENDING)
                    .excuseReason(excuseReason)
                    .build();
            }

            // 기존에 지각이였던 경우
            else if (this.attendance.getStatus() == AttendanceStatus.LATE) {
                this.attendance = ScheduleParticipantAttendance.builder()
                    .location(location)
                    .status(AttendanceStatus.LATE_EXCUSE_PENDING)
                    .isLocationVerified(isLocationVerified)
                    .excuseReason(excuseReason)
                    .build();
            }

            // 이외의 상태에서는 사유 제출이 불가능함.
            else {
                throw new ScheduleDomainException(ScheduleErrorCode.INVALID_ATTENDANCE_STATUS_FOR_EXCUSE);
            }
        }
    }

    /**
     * 출석을 승인하거나 사유 제출된 건에 대하여 승인하는 메소드
     * <p>
     * 기존에 승인 가능한 상태여야만 합니다.
     */
    public void approveAttendance(Long approvedByMemberId) {
        validateAttendanceStatusForConfirm();

        this.attendance.approve(approvedByMemberId);
    }

    public void rejectAttendance(Long rejectedByMemberId) {
        validateAttendanceStatusForConfirm();

        this.attendance.reject(rejectedByMemberId);
    }

    public void forceChangeAttendance(Long decidedMemberId, AttendanceStatus status) {
        validateAttendanceStatusForConfirm();

        this.attendance.forceChange(decidedMemberId, status);
    }

    private void throwIfAttendanceNotExists() {
        if (this.attendance == null) {
            throw new ScheduleDomainException(ScheduleErrorCode.NO_ATTENDANCE_RECORD);
        }
    }

    private void validateAttendanceStatusForConfirm() {
        throwIfAttendanceNotExists();

        if (!this.attendance.getStatus().isPending()) {
            throw new ScheduleDomainException(ScheduleErrorCode.ATTENDANCE_NOT_REQUIRES_CONFIRM);
        }
    }
}
