package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workbook_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkbookMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType missionType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    private WorkbookMission(String title,
                            MissionType missionType,
                            String content) {
        this.title = title;
        this.missionType = missionType;
        this.content = content;
    }

//    public void update(String title, MissionType missionType, String content) {
//        this.title = title;
//        this.missionType = missionType;
//        this.content = content;
//    }
}
