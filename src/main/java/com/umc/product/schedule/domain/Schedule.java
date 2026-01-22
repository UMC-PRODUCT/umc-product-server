package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleErrorCode;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "schedule_tags",
            joinColumns = @JoinColumn(name = "schedule_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    @BatchSize(size = 100)
    private Set<ScheduleTag> tags = new HashSet<>();

    @Column(nullable = false)
    private Long authorChallengerId;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    @Column(nullable = false)
    private boolean isAllDay;

    private String locationName;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Builder
    private Schedule(String name, String description, Set<ScheduleTag> tags,
                     Long authorChallengerId, LocalDateTime startsAt, LocalDateTime endsAt,
                     boolean isAllDay, String locationName, Point location) {
        this.name = name;
        this.description = description;
        this.tags = tags != null ? tags : new HashSet<>();
        this.authorChallengerId = authorChallengerId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.isAllDay = isAllDay;
        this.locationName = locationName;
        this.location = location;
    }

    public boolean isInProgress(LocalDateTime referenceTime) {
        return referenceTime.isAfter(startsAt) && referenceTime.isBefore(endsAt);
    }

    public String resolveStatus(LocalDateTime now) {
        if (this.isEnded(now)) {
            return "종료됨";
        }
        if (this.isInProgress(now)) {
            return "진행 중";
        }
        return "예정";
    }

    public boolean isEnded(LocalDateTime referenceTime) {
        return referenceTime.isAfter(endsAt);
    }

    public void update(
            String name,
            String description,
            Set<ScheduleTag> tags,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            Boolean isAllDay,
            String locationName,
            Point location
    ) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (tags != null) {
            this.tags.clear();
            this.tags.addAll(tags);
        }
        if (locationName != null) {
            this.locationName = locationName;
        }
        if (location != null) {
            this.location = location;
        }

        updateTime(startsAt, endsAt, isAllDay);
    }

    private void updateTime(LocalDateTime newStartsAt, LocalDateTime newEndsAt, Boolean newIsAllDay) {
        // 변경할 값이 없으면 기존 값 유지
        boolean effectiveIsAllDay = (newIsAllDay != null) ? newIsAllDay : this.isAllDay;
        LocalDateTime effectiveStartsAt = (newStartsAt != null) ? newStartsAt : this.startsAt;
        LocalDateTime effectiveEndsAt = (newEndsAt != null) ? newEndsAt : this.endsAt;

        // 종일 일정일 경우 시간 강제 조정 (00:00 ~ 23:59)
        if (effectiveIsAllDay) {
            LocalDate startDate = effectiveStartsAt.toLocalDate();
            LocalDate endDate = effectiveEndsAt.toLocalDate();
            effectiveStartsAt = startDate.atStartOfDay();
            effectiveEndsAt = endDate.atTime(LocalTime.of(23, 59, 59));
        }

        if (effectiveStartsAt.isAfter(effectiveEndsAt)) {
            throw new BusinessException(Domain.SCHEDULE, ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        this.isAllDay = effectiveIsAllDay;
        this.startsAt = effectiveStartsAt;
        this.endsAt = effectiveEndsAt;
    }

    public boolean hasTag(ScheduleTag tag) {
        return this.tags.contains(tag);
    }
}
