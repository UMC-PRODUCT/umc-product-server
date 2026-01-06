package com.umc.product.challenger.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenger_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengerMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // WorkbookMissionId
    private Long missionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_workbook_id", nullable = false)
    private ChallengerWorkbook challengerWorkbook;

    @Column(columnDefinition = "TEXT")
    private String submission;

    @Builder
    private ChallengerMission(Long missionId, ChallengerWorkbook challengerWorkbook, String submission) {
        this.missionId = missionId;
        this.challengerWorkbook = challengerWorkbook;
        this.submission = submission;
    }
}
