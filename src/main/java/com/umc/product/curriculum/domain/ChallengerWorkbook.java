package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
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
    /// /    private Long challengerId;
    private Long memberId;

    @Column(name = "study_group_id")
    private Long studyGroupId; // 워크북 강제 배포등을 고려하여 nullable로 함.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_workbook_id", nullable = false)
    private OriginalWorkbook originalWorkbook;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private WorkbookStatus status;

//    @Column
//    private Long scheduleId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isExcused; // true일시 과제 제출 여부와 무관하게 PASS처리

    private String excusedReason;

    private Long excuseApprovedMemberId;

//    @Column(columnDefinition = "TEXT")
//    private String feedback;
//
//    @Column(columnDefinition = "TEXT")
//    private String bestReason;
//
//    @Column(columnDefinition = "TEXT")
//    private String submission;

    @Builder(access = AccessLevel.PRIVATE)
    private ChallengerWorkbook(
        OriginalWorkbook originalWorkbook
    ) {
        this.originalWorkbook = originalWorkbook;
//        this.scheduleId = scheduleId;
//        this.status = status != null ? status : WorkbookStatus.PENDING;
    }

    public static ChallengerWorkbook create(


    ) {
        return null;
    }

}
