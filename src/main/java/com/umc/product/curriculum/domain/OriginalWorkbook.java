package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_curriculum_id", nullable = false)
    private WeeklyCurriculum weeklyCurriculum;

    @Column(name = "title", nullable = false)
    private String title;

    // 워크북에 대한 간단한 설명
    @Column(name = "description")
    private String description;

    @Column(name = "url")
    private String url;

    //    OriginalWorkbookStatus이 DRAFT, FINAL이 있다면 FINAL만 release할 수 있도록 할 지?
    //    주차를 한번에 release하게할 까요 originalWorkbook을 하나씩 release하게 할까 고민
    @Enumerated(EnumType.STRING)
    @Column(name = "original_workbook_status", nullable = false)
    private OriginalWorkbookStatus originalWorkbookStatus;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    private Instant releasedAt;

    private Long releasedMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OriginalWorkbookType type;

    @Builder(access = AccessLevel.PRIVATE)
    private OriginalWorkbook(
        WeeklyCurriculum weeklyCurriculum,
        String title, String description,
        String url, String content
    ) {
        this.weeklyCurriculum = weeklyCurriculum;
        this.title = title;
        this.description = description;
        this.url = url;
        this.content = content;
    }

    public static OriginalWorkbook create(WeeklyCurriculum weeklyCurriculum, String title, String description,
                                          String url, String content) {
        return OriginalWorkbook.builder()
            .weeklyCurriculum(weeklyCurriculum)
            .title(title)
            .description(description)
            .url(url)
            .content(content)
            .build();
    }
}
