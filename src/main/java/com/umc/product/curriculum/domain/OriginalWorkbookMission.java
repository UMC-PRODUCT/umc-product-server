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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "original_workbook_mission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OriginalWorkbookMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_workbook_id", nullable = false)
    private OriginalWorkbook originalWorkbook;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType missionType;

    @Column(nullable = false)
    private boolean isNecessary;


}
