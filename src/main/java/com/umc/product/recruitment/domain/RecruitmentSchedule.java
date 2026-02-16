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

    /**
     * 추가 모집(Extension)을 위한 일정 복제 메서드
     */
    public static RecruitmentSchedule copyForExtension(Long newRecruitmentId, RecruitmentSchedule source) {
        return RecruitmentSchedule.builder()
            .recruitmentId(newRecruitmentId) // 새로운 추가 모집 ID 부여
            .type(source.getType())          // 동일한 일정 타입 (예: 면접 진행)
            .startsAt(source.getStartsAt())  // 기존의 시작 시간 복제
            .endsAt(source.getEndsAt())      // 기존의 종료 시간 복제
            .note(source.getNote())          // 비고 사항 복제
            .build();
    }

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
