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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "challenger_workbook",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_challenger_workbook_member_id_original_workbook_id",
        columnNames = {"member_id", "original_workbook_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengerWorkbook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "study_group_id")
    private Long studyGroupId; // 워크북 강제 배포등을 고려하여 nullable로 함.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_workbook_id", nullable = false)
    private OriginalWorkbook originalWorkbook;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isExcused; // true일시 과제 제출 여부와 무관하게 PASS처리

    private String excusedReason;
    private Long excuseApprovedMemberId;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerWorkbook(
        OriginalWorkbook originalWorkbook
    ) {
        this.originalWorkbook = originalWorkbook;
    }

    public static ChallengerWorkbook create(
    ) {
        return null;
    }

}
