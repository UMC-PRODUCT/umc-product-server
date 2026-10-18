package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.chapter.ChapterInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMatchingAffiliationInfo;

public record ProjectMatchingAffiliationResponse(
    Long gisuId,
    ChapterSummary originChapter,
    ChapterSummary matchingChapter,
    boolean transferred
) {

    public record ChapterSummary(Long id, String name) {

        static ChapterSummary from(ChapterInfo info) {
            return info == null ? null : new ChapterSummary(info.id(), info.name());
        }
    }

    public static ProjectMatchingAffiliationResponse from(ProjectMatchingAffiliationInfo info) {
        return new ProjectMatchingAffiliationResponse(
            info.gisuId(),
            ChapterSummary.from(info.originChapter()),
            ChapterSummary.from(info.matchingChapter()),
            info.transferred()
        );
    }
}
