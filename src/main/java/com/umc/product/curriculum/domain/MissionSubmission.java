package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mission_submission")
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


}
