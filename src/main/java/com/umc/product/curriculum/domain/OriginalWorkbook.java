package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
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

    @Column(name = "weekly_curriculum_id", nullable = false)
    private Long weeklyCurriculumId;

    @Column(name = "title", nullable = false)
    private String title;

    // 워크북에 대한 간단한 설명
    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "description", nullable = true)
    private String url;

    @Column(name= "content", nullable = false)
    private String content;

//    OriginalWorkbookStatus이 DRAFT, FINAL이 있다면 FINAL만 release할 수 있도록 할 지?
//    주차를 한번에 release하게할 까요 originalWorkbook을 하나씩 release하게 할까 고민
//    @Column(name = "original_workbook_status", nullable = false)
//    private OriginalWorkbookStatus originalWorkbookStatus;

    private Instant releasedAt;

    @OneToMany(mappedBy = "originalWorkbook", orphanRemoval = true, cascade = CascadeType.ALL)
    OriginalWorkbookMission originalWorkbookMission;

    @Builder(access = AccessLevel.PRIVATE)
    private OriginalWorkbook(Long weeklyCurriculumId, String title, String description, String url, String content) {
        this.weeklyCurriculumId = weeklyCurriculumId;
        this.title = title;
        this.description = description;
        this.url = url;
        this.content = content;
    }

     public static OriginalWorkbook create(Long weeklyCurriculumId, String title, String description, String url, String content) {
        return OriginalWorkbook.builder()
                .weeklyCurriculumId(weeklyCurriculumId)
                .title(title)
                .description(description)
                .url(url)
                .content(content)
                .build();
    }


    /**
     * 워크북 배포
     */
//    public void release() {
//        this.releasedAt = Instant.now();
//    }

    /**
     * 워크북 배포 여부 확인
     */
//    public boolean isReleased() {
//        return this.releasedAt != null;
//    }

}
