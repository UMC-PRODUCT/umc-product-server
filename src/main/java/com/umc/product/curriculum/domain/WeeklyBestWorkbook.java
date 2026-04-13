package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "weekly_best_workbook",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_weekly_best_workbook_member_week_study_group",
            columnNames = {
                "member_id", "study_group_id", "weekly_curriculum_id"
            })
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyBestWorkbook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "study_group_id")
    private Long studyGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_curriculum_id", nullable = false)
    private WeeklyCurriculum weeklyCurriculum;

    @Column(name = "best_reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private Long decidedMemberId;

    // 베스트 워크북 선정 시 상점 부여는 Service 단에서 자동으로 묶어둘것 !
}
