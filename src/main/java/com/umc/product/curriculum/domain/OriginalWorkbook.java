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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String workbookUrl;

    @Column(nullable = false)
    private Integer orderNo;

    private LocalDateTime releasedAt;

    @Builder
    private OriginalWorkbook(Curriculum curriculum, String title, String description,
                             String workbookUrl, Integer orderNo) {
        this.curriculum = curriculum;
        this.title = title;
        this.description = description;
        this.workbookUrl = workbookUrl;
        this.orderNo = orderNo;
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
