package com.umc.product.challenger.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(nullable = false)
    private Long missionId;

    @Column(nullable = false)
    private Long challengerWorkbookId;

    @Column(columnDefinition = "TEXT")
    private String submission;

    @Builder
    private ChallengerMission(Long missionId, Long challengerWorkbookId, String submission) {
        this.missionId = missionId;
        this.challengerWorkbookId = challengerWorkbookId;
        this.submission = submission;
    }
}
