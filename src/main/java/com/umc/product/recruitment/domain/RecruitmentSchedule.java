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
import java.util.Objects;
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

    public boolean isSamePeriod(RecruitmentSchedule other) {
        if (other == null) {
            return false;
        }

        // 시작 시간과 종료 시간이 모두 일치하는지 확인 (null 포함 비교)
        return Objects.equals(this.startsAt, other.getStartsAt()) &&
            Objects.equals(this.endsAt, other.getEndsAt());
    }

    /**
     * 현재 시각 기준으로 해당 일정이 '활성화' 상태인지 확인합니다. - WINDOW: 시작 시각(포함)과 종료 시각(미포함) 사이일 때 - AT: 시작 시각(발표 시각 등)을 지났을 때 (정각 포함)
     */
    public boolean isActive(Instant now) {
        if (now == null) {
            return false;
        }

        if (type.isWindow()) {
            // 시작과 종료가 모두 있어야 하며, 그 사이일 때 활성
            if (startsAt == null || endsAt == null) {
                return false;
            }
            return !now.isBefore(startsAt) && now.isBefore(endsAt);
        }

        // AT 타입: 시작 시각(기준점)만 있으면 되며, 그 시점을 지났을 때 활성
        if (startsAt == null) {
            return false;
        }
        return !now.isBefore(startsAt);
    }

    /**
     * 현재 시각 기준으로 해당 단계가 완전히 '종료'되었는지 확인합니다. (대시보드 상태바 제어용) * - WINDOW: 종료 시각(endsAt)이 되는 그 찰나(정각)부터 '지나간 것'으로 간주합니다.
     * (now >= endsAt) - AT: 시작 시각(발표 시각 등)이 되는 그 순간 '사건 발생 완료'로 간주합니다. (now >= startsAt)
     */
    public boolean isPassed(Instant now) {
        if (now == null) {
            return false;
        }

        if (type.isWindow()) {
            // isActive는 종료 정각 미포함(< endsAt)
            // isPassed는 정각 포함(>= endsAt) (상태의 공백이 없도록)
            return endsAt != null && !now.isBefore(endsAt);
        }

        // AT 타입: 선언된 기준 시각이 되는 순간부터 해당 일정은 완료된 상태입니다.
        return startsAt != null && !now.isBefore(startsAt);
    }

    /**
     * 현재 시각 기준으로 해당 일정이 이미 시작되었는지 확인합니다. - WINDOW/AT 공통: 시작 시각(startsAt)이 되는 그 찰나(정각)부터 '시작된 것'으로 간주합니다. (now >=
     * startsAt) - 발행된 모집의 시작일 수정 제한(Frozen)이나, 특정 단계의 진행 여부를 판단할 때 사용합니다.
     */
    public boolean isStarted(Instant now) {
        if (now == null || startsAt == null) {
            return false;
        }
        return !now.isBefore(startsAt); // 정각 포함 시작 여부
    }
}
