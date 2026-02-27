package com.umc.product.recruitment.domain.enums;

public enum RecruitmentStatus {
    DRAFT, PUBLISHED;

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }
}
