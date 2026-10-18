package com.umc.product.project.application.port.in.query.dto;

import java.time.Instant;

import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

/**
 * 매칭 지부 이동 결과.
 *
 * @param cancelledApplicationCount 이동·해제 처리 중 자동 철회된 지원서 수
 */
public record ProjectMatchingChapterTransferInfo(
    Long transferId,
    Long gisuId,
    Long memberId,
    Long fromChapterId,
    Long toChapterId,
    String reason,
    Instant releasedAt,
    int cancelledApplicationCount
) {

    public static ProjectMatchingChapterTransferInfo of(
        ProjectMatchingChapterTransfer transfer, int cancelledApplicationCount
    ) {
        return new ProjectMatchingChapterTransferInfo(
            transfer.getId(),
            transfer.getGisuId(),
            transfer.getMemberId(),
            transfer.getFromChapterId(),
            transfer.getToChapterId(),
            transfer.getReason(),
            transfer.getReleasedAt(),
            cancelledApplicationCount
        );
    }
}
