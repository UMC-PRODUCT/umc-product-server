package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;
import java.util.List;

/**
 * 시간창 기반 figma 댓글 요약/발송의 단일 응답 (ADR-004 §Decision 1·2).
 * <p>
 * sync / digest / preview 세 진입점이 동일한 grouping 형태로 응답해, 운영진이 일관된 모양으로 검증할 수 있다.
 *
 * @param from                          처리한 시간창 시작
 * @param to                            처리한 시간창 끝
 * @param totalComments                 시간창 안에 들어온 댓글 총합 (발송 여부 무관, 라우팅 매칭 성공한 것만 카운트)
 * @param unmatchedCount                분류 매칭 실패 + fallback 도메인도 없어 묶이지 못한 댓글 수
 * @param skippedAlreadyDispatchedCount force=false 모드에서 이미 dispatch 행이 있어 발송 건너뛴 댓글 수
 * @param domains                       도메인별 묶음 결과
 */
public record FigmaSummaryResult(
    Instant from,
    Instant to,
    int totalComments,
    int unmatchedCount,
    int skippedAlreadyDispatchedCount,
    List<DomainGroup> domains
) {

    public static FigmaSummaryResult empty(Instant from, Instant to) {
        return new FigmaSummaryResult(from, to, 0, 0, 0, List.of());
    }

    /**
     * @param sent Discord 발송 성공 여부. dryRun 모드 / 발송 대상 0건이면 false.
     */
    public record DomainGroup(
        String domainKey,
        String webhookUrl,
        boolean fallback,
        List<String> mentionRenders,
        boolean sent,
        List<Comment> comments
    ) {
    }

    /**
     * @param alreadyDispatched 본 댓글이 이미 dispatch 행을 가진 (= 과거에 발송된) 댓글인지. force 모드에서는 true 라도 발송 대상에 포함된다.
     */
    public record Comment(
        String commentId,
        String message,
        String authorName,
        String fileKey,
        String fileDisplayName,
        String nodeId,
        String pageName,
        String classifiedDomainKey,
        Instant createdAt,
        boolean alreadyDispatched
    ) {
    }
}
