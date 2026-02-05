package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.ScheduleTag;
import com.umc.product.schedule.domain.exception.ScheduleDomainException;
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
import java.time.Instant;
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
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Column(nullable = false)
    private boolean isAllDay;

    private String locationName;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    private Long studyGroupId;

    @Builder
    private Schedule(String name, String description, Set<ScheduleTag> tags,
                     Long authorChallengerId, Instant startsAt, Instant endsAt,
                     boolean isAllDay, String locationName, Point location) {
        this.name = name;
        this.description = description;

        if (tags == null || tags.isEmpty()) {
            throw new ScheduleDomainException(ScheduleErrorCode.TAG_REQUIRED);
        }
        this.tags = new HashSet<>(tags);

        this.authorChallengerId = authorChallengerId;

        if (startsAt.isAfter(endsAt)) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
        this.startsAt = startsAt;
        this.endsAt = endsAt;

        this.isAllDay = isAllDay;
        this.locationName = locationName;
        this.location = location;
        this.studyGroupId = studyGroupId;
    }

    public boolean isInProgress(Instant referenceTime) {
        return referenceTime.isAfter(startsAt) && referenceTime.isBefore(endsAt);
    }

    public String resolveStatus(Instant now) {
        if (this.isEnded(now)) {
            return "종료됨";
        }
        if (this.isInProgress(now)) {
            return "진행 중";
        }
        return "예정";
    }

    public boolean isEnded(Instant referenceTime) {
        return referenceTime.isAfter(endsAt);
    }

    public void update(
        String name,
        String description,
        Set<ScheduleTag> tags,
        Instant startsAt,
        Instant endsAt,
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
            if (tags.isEmpty()) {
                throw new ScheduleDomainException(ScheduleErrorCode.TAG_REQUIRED);
            }
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

    public void updateLocation(
        String locationName,
        Point location
    ) {
        if (locationName != null) {
            this.locationName = locationName;
        }
        if (location != null) {
            this.location = location;
        }
    }

    private void updateTime(Instant newStartsAt, Instant newEndsAt, Boolean newIsAllDay) {
        // 변경할 값이 없으면 기존 값 유지
        boolean effectiveIsAllDay = (newIsAllDay != null) ? newIsAllDay : this.isAllDay;
        Instant effectiveStartsAt = (newStartsAt != null) ? newStartsAt : this.startsAt;
        Instant effectiveEndsAt = (newEndsAt != null) ? newEndsAt : this.endsAt;

        if (effectiveStartsAt.isAfter(effectiveEndsAt)) {
            throw new ScheduleDomainException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }

        this.isAllDay = effectiveIsAllDay;
        this.startsAt = effectiveStartsAt;
        this.endsAt = effectiveEndsAt;
    }

    public boolean hasTag(ScheduleTag tag) {
        return this.tags.contains(tag);
    }

    public void assignStudyGroup(Long studyGroupId) {
        this.studyGroupId = studyGroupId;
    }
}
