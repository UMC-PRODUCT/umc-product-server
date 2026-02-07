package com.umc.product.recruitment.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecruitmentSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recruitment_id", nullable = false)
    private Long recruitmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentScheduleType type;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column
    private String note; // 표시용 문구. 추후 UI 보고 필요 없다 판단되면 삭제

    public static RecruitmentSchedule createDraft(Long recruitmentId, RecruitmentScheduleType type) {
        return RecruitmentSchedule.builder()
            .recruitmentId(recruitmentId)
            .type(type)
            .startsAt(null)
            .endsAt(null)
            .note(null)
            .build();
    }

    public void changePeriod(Instant startsAt, Instant endsAt) {
        if (startsAt != null) {
            this.startsAt = startsAt;
        }
        if (endsAt != null) {
            this.endsAt = endsAt;
        }
    }

    public boolean canChangeStartNotAdvanced(Instant newStartAt) {
        if (newStartAt == null || this.startsAt == null) {
            return true;
        }
        return !newStartAt.isBefore(this.startsAt); // 면접 일정 앞당기기 금지
    }

    public boolean canChangeEndNotShortened(Instant newEndAt) {
        if (newEndAt == null || this.endsAt == null) {
            return true;
        }
        return !newEndAt.isBefore(this.endsAt); // 단축 금지
    }

    public void changeAt(Instant at) {
        if (at != null) {
            this.startsAt = at;
        }
    }

    public static RecruitmentSchedule create(
        Long recruitmentId,
        RecruitmentScheduleType type,
        Instant startsAt,
        Instant endsAt
    ) {
        return RecruitmentSchedule.builder()
            .recruitmentId(recruitmentId)
            .type(type)
            .startsAt(startsAt)
            .endsAt(endsAt)
            .note(null)
            .build();
    }

    public static RecruitmentSchedule createAt(
        Long recruitmentId,
        RecruitmentScheduleType type,
        Instant at
    ) {
        return RecruitmentSchedule.builder()
            .recruitmentId(recruitmentId)
            .type(type)
            .startsAt(at)
            .endsAt(null)
            .note(null)
            .build();
    }

}
