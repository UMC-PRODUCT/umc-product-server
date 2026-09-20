package com.umc.product.curriculum.domain;

import com.umc.product.common.BaseEntity;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gisuId;

    @Enumerated(EnumType.STRING)
    private ChallengerPart part;

    @Column(nullable = false)
    private String title;

    @Builder(access = AccessLevel.PRIVATE)
    private Curriculum(Long gisuId, ChallengerPart part, String title) {
        if (part == null) {
            throw new CurriculumDomainException(CurriculumErrorCode.INVALID_CURRICULUM_LEARNING_TYPE);
        }
        this.gisuId = gisuId;
        this.part = part;
        this.title = title;
    }

    public static Curriculum create(Long gisuId, ChallengerPart part, String title) {
        return Curriculum.builder().gisuId(gisuId).part(part).title(title).build();
    }

    public void updateTitle(String title) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
    }

}
