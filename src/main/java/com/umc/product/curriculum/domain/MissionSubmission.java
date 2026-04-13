package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.MissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "mission_submission",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_mission_submission_challenger_mission_id",
            columnNames = {"", "challenger_mission_id"}
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_workbook_mission_id", nullable = false)
    private OriginalWorkbookMission originalWorkbookMission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_mission_id", nullable = false)
    private ChallengerMission challengerMission;

    @Column(name = "submitted_mission_as_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MissionType submittedAsType;

    // 고민: MissionType가 PLAIN 일 때, 즉 단순 제출일 떄를 위한 필드를 추가할 것인지
    // 아니면 content를 nullable로 둘 것인지?

    @Column(columnDefinition = "TEXT")
    private String content;


}
