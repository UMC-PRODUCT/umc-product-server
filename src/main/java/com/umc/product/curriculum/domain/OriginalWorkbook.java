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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "original_workbook")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginalWorkbook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Column(nullable = false)
    private String title;

    private String description;

    private String workbookUrl;

    @Column(nullable = false)
    private Integer weekNo;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType missionType;

    private LocalDateTime releasedAt;

    @Builder
    private OriginalWorkbook(Curriculum curriculum, String title, String description,
                             String workbookUrl, Integer weekNo, LocalDate startDate,
                             LocalDate endDate, MissionType missionType) {
        this.curriculum = curriculum;
        this.title = title;
        this.description = description;
        this.workbookUrl = workbookUrl;
        this.weekNo = weekNo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.missionType = missionType;
        this.releasedAt = null;
    }

    /**
     * 워크북 배포
     */
    public void release() {
        this.releasedAt = LocalDateTime.now();
    }

    /**
     * 워크북 배포 여부 확인
     */
    public boolean isReleased() {
        return this.releasedAt != null;
    }

    /**
     * 워크북 URL 업데이트
     */
    public void updateWorkbookUrl(String workbookUrl) {
        this.workbookUrl = workbookUrl;
    }
}
