package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;
import java.util.List;

/**
 * digest 결과 응답. 발송된 도메인별로 댓글 수와 발송 성공 여부를 포함한다.
 */
public record FigmaDigestSummary(
    Instant from,
    Instant to,
    int totalComments,
    int unmatchedCount,
    List<DomainResult> domains
) {
    public record DomainResult(
        String domainKey,
        int commentCount,
        boolean sent
    ) {
    }
}
