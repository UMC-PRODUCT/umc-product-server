package com.umc.product.curriculum.domain;

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
@Table(name = "original_workbook")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OriginalWorkbook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long curriculumId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String workbookUrl;

    @Column(nullable = false)
    private Integer orderNo;

    @Builder
    private OriginalWorkbook(Long curriculumId, String title, String description,
                             String workbookUrl, Integer orderNo) {
        this.curriculumId = curriculumId;
        this.title = title;
        this.description = description;
        this.workbookUrl = workbookUrl;
        this.orderNo = orderNo;
    }

//    public void update(String title, String description, String workbookUrl, Integer orderNo) {
//        this.title = title;
//        this.description = description;
//        this.workbookUrl = workbookUrl;
//        this.orderNo = orderNo;
//    }
}
