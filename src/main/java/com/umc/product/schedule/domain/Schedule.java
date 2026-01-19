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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false)
    private boolean isAllDay;

    private String locationName;

    @Builder
    private Schedule(String name, String description, ScheduleType type,
                     Long authorChallengerId, LocalDateTime startsAt, LocalDateTime endsAt,
                     boolean isAllDay, String locationName) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.authorChallengerId = authorChallengerId;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.isAllDay = isAllDay;
        this.locationName = locationName;
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
}
