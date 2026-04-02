package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
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
@Table(name = "challenger_workbook_submission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long challengerWorkbookId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder(access = AccessLevel.PRIVATE)
    private Submission(Long challengerWorkbookId, String content) {
        this.challengerWorkbookId = challengerWorkbookId;
        this.content = content;
    }

    public static Submission create(Long challengerWorkbookId, String content) {
        return Submission.builder()
            .challengerWorkbookId(challengerWorkbookId)
            .content(content)
            .build();
    }

    public void updateContent(MissionType missionType, String content) {
        validateContent(missionType, content);
        this.content = content;
    }

    private void validateContent(MissionType missionType, String content) {
        if (missionType != MissionType.PLAIN && (content == null || content.isBlank())) {
            throw new CurriculumDomainException(CurriculumErrorCode.SUBMISSION_REQUIRED);
        }
    }
}
