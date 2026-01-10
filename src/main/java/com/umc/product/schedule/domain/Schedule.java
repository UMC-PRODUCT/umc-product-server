package com.umc.product.schedule.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.schedule.domain.enums.ScheduleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleType type;

    @Column(nullable = false)
    private Long authorChallengerId;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    private String locationName;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column(nullable = false)
    private Duration lateThreshold;

    @Column(nullable = false)
    private Duration absentThreshold;

    @Column(nullable = false)
    private Integer attendanceRadius;

    @Builder
    private Schedule(String name, String description, ScheduleType type,
                     Long authorChallengerId, LocalDateTime startsAt, LocalDateTime endsAt,
                     String locationName, Point location,
                     Duration lateThreshold, Duration absentThreshold, Integer attendanceRadius) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.authorChallengerId = authorChallengerId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.locationName = locationName;
        this.location = location;
        this.lateThreshold = lateThreshold != null ? lateThreshold : Duration.ofMinutes(10);
        this.absentThreshold = absentThreshold != null ? absentThreshold : Duration.ofMinutes(30);
        this.attendanceRadius = attendanceRadius != null ? attendanceRadius : 50;
    }

    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    public boolean isInProgress(LocalDateTime referenceTime) {
        return referenceTime.isAfter(startsAt) && referenceTime.isBefore(endsAt);
    }

    public boolean isEnded(LocalDateTime referenceTime) {
        return referenceTime.isAfter(endsAt);
    }
}
