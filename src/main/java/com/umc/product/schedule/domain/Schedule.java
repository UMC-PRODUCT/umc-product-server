package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.AttendanceStatus;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 복합키로 생성되어, 별도로 PK가 없는 매핑 테이블 생성을 위해서 CollectionTable 사용
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "schedule_tags",
        joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    @BatchSize(size = 100)
    private Set<ScheduleTag> tags = new HashSet<>();

    @Column(name = "author_member_id", nullable = false)
    private Long authorMemberId;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    private Point location; // nullable, 비대면 일정의 경우에 없습니다!

    @Embedded
    // policy가 존재하면 출석을 체크하는 일정, 즉 출석부를 생성하는 일정임
    // 출석부을 트래킹하지 않도록 변경하고 싶다면 policy를 삭제하면 됨.
    private AttendancePolicy policy;

    // 삭제: authorChallengerId

    // 삭제: isAllDay
    // 클라이언트 입장에서 기기 시각 기준 00:00에 시작해서, 23:59에 끝나는 일정이면 알아서 "종일"로 표기하기

    // 삭제: studyGroupId
    // 이건 따로 Entity 만들어서 관리합니다.
    // Schedule에 둘까 Organization에 둘까 영원한 고민
    // => Organization에 두도록 합니다. Schedule 도메인의 순수성을 지키고자 ..

    // 생성자에서 AttendancePolicy에 있는 값은 3개 다 주어지거나 아예 안 주어지거나 둘 중 하나로 해야합니다!

    // TODO: Builder 생성자 쓰는 곳 수정 유의해주세요
    @Builder
    private Schedule(
        String name, String description, Set<ScheduleTag> tags,
        Long authorMemberId, Instant startsAt, Instant endsAt,
        String locationName, Point location,
        AttendancePolicy policy
    ) {
        // 일정 관련 기본 정보들
        this.name = name;
        this.description = description;

        validateScheduleTags(tags);
        this.tags = new HashSet<>(tags);

        this.authorMemberId = authorMemberId;

        validateScheduleTime(policy, startsAt, endsAt);
        this.startsAt = startsAt;
        this.endsAt = endsAt;

        this.locationName = locationName;
        this.location = location;

        this.policy = policy;
    }

    // validate method가 더 많아지고 복잡해질 경우 하나의 validate()로 변경할 것

    private static void validateScheduleTags(Set<ScheduleTag> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new ScheduleDomainException(ScheduleErrorCode.TAG_REQUIRED);
        }
    }

    // 일정 시작, 종료 시간
    // (1) 종료 시간은 시작 시간보다 앞서서는 안됨
    // (2) policy가 있는 경우, 일정 종료 시각은 시작시간 + 출석 인정 시간 + 지각 인정 시간 보다 늦어야 함
    private static void validateScheduleTime(AttendancePolicy policy, Instant start, Instant end) {
        if (end.isBefore(start)) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE,
                "일정 종료 시각은 시작 시각보다 늦어야 합니다");
        }

        // policy가 없으면 출석 체크를 하지 않는 일정이므로 추가 검증 불필요
        if (policy == null) {
            return;
        }

        long totalLateMinutes =
            policy.getAttendanceGraceMinutes() + policy.getLateToleranceMinutes();

        if (end.isBefore(start.plus(totalLateMinutes, ChronoUnit.MINUTES))) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE,
                "일정 종료 시각은 시작 시각 + 출석 인정 시간 + 지각 인정 시간 보다 늦어야 합니다");
        }
    }

    public boolean isInProgress(Instant referenceTime) {
        return referenceTime.isAfter(startsAt) && referenceTime.isBefore(endsAt);
    }

    public boolean isEnded(Instant referenceTime) {
        return referenceTime.isAfter(endsAt);
    }

    // 요청을 받은 시각이 어떤 AttendanceStatus에 해당하는지 판단하는 메소드

    /**
     * 최초 일정 출석 요청 시, 어떤 상태로 마킹해야 하는지 판단하는 메소드입니다.
     */
    public AttendanceStatus getAttendanceStatus() {
        Instant now = Instant.now();

        if (now.isAfter(this.endsAt)) {
            throw new ScheduleDomainException(
                ScheduleErrorCode.SCHEDULE_ENDED,
                this.name + " 일정은 " + this.endsAt + " 에 이미 종료된 일정입니다."
            );
        }

        return this.policy.getAttendanceStatusByPolicy(
            Instant.now(), this.startsAt
        );
    }

    public static AttendancePolicy createAttendancePolicy(
        Instant checkInStartAt, // 출석 요청 시작 가능 시점
        Instant onTimeEndAt, // 출석으로 인정하는 마감 시간
        Instant lateEndAt, // 지각으로 인정하는 마감 시간,
        Instant startsAt, // 일정 시작 시간
        Instant endsAt // 일정 종료 시간
    ) {
        validateAttendancePolicyTimes(checkInStartAt, onTimeEndAt, lateEndAt, startsAt, endsAt);

        Long earlyCheckInMinutes = ChronoUnit.MINUTES.between(checkInStartAt, startsAt);
        Long attendanceGraceMinutes = ChronoUnit.MINUTES.between(startsAt, onTimeEndAt);
        Long lateToleranceMinutes = ChronoUnit.MINUTES.between(onTimeEndAt, lateEndAt);

        return AttendancePolicy.create(earlyCheckInMinutes, attendanceGraceMinutes, lateToleranceMinutes);
    }

    private static void validateAttendancePolicyTimes(
        Instant checkInStartAt, // 출석 요청 시작 가능 시점
        Instant onTimeEndAt, // 출석으로 인정하는 마감 시간
        Instant lateEndAt, // 지각으로 인정하는 마감 시간,
        Instant startsAt, // 일정 시작 시간
        Instant endsAt // 일정 종료 시간
    ) {
        // 검증해야 하는 내용: checkInStartAt < startAt < onTimeEndAt < lateEndAt < endsAt
        if (!checkInStartAt.isBefore(startsAt) ||
            !startsAt.isBefore(onTimeEndAt) ||
            !onTimeEndAt.isBefore(lateEndAt) ||
            !lateEndAt.isBefore(endsAt)
        ) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }

    // 일정 수정 메서드
    // null인 필드는 기존 값 유지
    public void update(
        String name,
        String description,
        Set<ScheduleTag> tags,
        Instant startsAt,
        Instant endsAt,
        String locationName,
        Point location,
        AttendancePolicy policy
    ) {
        // 단순 필드 - null이면 기존 값 유지
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (locationName != null) {
            this.locationName = locationName;
        }
        if (location != null) {
            this.location = location;
        }

        // 태그 : 검증 필요
        if (tags != null) {
            validateScheduleTags(tags);
            this.tags = new HashSet<>(tags);
        }

        // 일정 시간/출석 정책 : 연관 검증 필요
        boolean isTimeChanged = startsAt != null || endsAt != null;
        boolean isPolicyChanged = policy != null;

        if (isTimeChanged || isPolicyChanged) {
            Instant finalStartsAt = startsAt != null ? startsAt : this.startsAt;
            Instant finalEndsAt = endsAt != null ? endsAt : this.endsAt;
            AttendancePolicy finalPolicy = policy != null ? policy : this.policy;

            validateScheduleTime(finalPolicy, finalStartsAt, finalEndsAt);

            this.startsAt = finalStartsAt;
            this.endsAt = finalEndsAt;
            this.policy = finalPolicy;
        }
    }

    // 비대면 일정으로 전환 (대면 -> 비대면)
    // location만 삭제됨, policy는 유지 (출석 체크 여부는 독립적)
    public void convertToOnline() {
        this.location = null;
        this.locationName = null;
    }

    // 출석을 요하지 않는 일정으로 변환 시 사용
    public void removeAttendancePolicy() {
        this.policy = null;
    }
}
