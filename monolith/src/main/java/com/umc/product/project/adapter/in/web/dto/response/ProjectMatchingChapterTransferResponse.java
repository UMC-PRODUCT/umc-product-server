package com.umc.product.project.adapter.in.web.dto.response;

import java.time.Instant;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingChapterTransferInfo;

public record ProjectMatchingChapterTransferResponse(
    Long transferId,
    Long gisuId,
    Long memberId,
    Long fromChapterId,
    Long toChapterId,
    String reason,
    Instant releasedAt,
    int cancelledApplicationCount
) {

    public static ProjectMatchingChapterTransferResponse from(ProjectMatchingChapterTransferInfo info) {
        return new ProjectMatchingChapterTransferResponse(
            info.transferId(),
            info.gisuId(),
            info.memberId(),
            info.fromChapterId(),
            info.toChapterId(),
            info.reason(),
            info.releasedAt(),
            info.cancelledApplicationCount()
        );
    }
}
