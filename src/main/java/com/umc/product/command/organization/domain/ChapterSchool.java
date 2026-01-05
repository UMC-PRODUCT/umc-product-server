package com.umc.product.command.organization.domain;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.command.organization.exception.OrganizationErrorCode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChapterSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Builder
    private ChapterSchool(Chapter chapter, School school) {
        validate(chapter, school);
        this.chapter = chapter;
        this.school = school;
    }

    private static void validate(Chapter chapter, School school) {
        if (chapter == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.CHAPTER_REQUIRED);
        }
        if (school == null) {
            throw new BusinessException(Domain.COMMON, OrganizationErrorCode.SCHOOL_REQUIRED);
        }
    }
}
