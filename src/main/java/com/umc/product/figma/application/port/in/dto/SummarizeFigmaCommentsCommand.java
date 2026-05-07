package com.umc.product.figma.application.port.in.dto;

import java.time.Instant;

/**
 * 시간창 기반 figma 댓글 요약/발송의 단일 입력 (ADR-004 §Decision 1·2).
 * <p>
 * 세 진입점 (스케줄러 sync / admin digest / admin preview) 은 본 record 의 정적 팩토리로
 * 정해진 플래그 조합을 사용해 동일한 본체 ({@code FigmaCommentSummaryService}) 를 호출한다.
 *
 * @param from            시간창 시작 (inclusive). null 이면 무한 과거.
 * @param to              시간창 끝 (inclusive). null 이면 무한 미래.
 * @param singleFileId    null 이면 활성 watched file 전체를 대상으로 한다. 비어있지 않으면 해당 파일 1건만
 *                        대상으로 하며, enabled 여부와 무관하게 처리한다 (운영진 on-demand 트리거 / preview 용).
 * @param dryRun          true → Discord 발송 / dispatch 기록 / cursor advance 모두 건너뛰고 묶음 결과만 반환.
 *                        preview 진입점이 사용한다.
 * @param force           true → dispatch 행이 있는 commentId 도 발송 대상에 포함한다.
 *                        admin digest 가 사용한다 (catch-up / 회고용).
 * @param advanceCursor   true → 발송이 완료된 후 figma_summary_cursor.last_window_end 를 to 로 advance.
 *                        스케줄러 sync 진입점만 사용한다.
 */
public record SummarizeFigmaCommentsCommand(
    Instant from,
    Instant to,
    Long singleFileId,
    boolean dryRun,
    boolean force,
    boolean advanceCursor
) {

    /** 스케줄러 sync 진입점: dispatch dedup 적용, dispatch 기록, cursor advance. */
    public static SummarizeFigmaCommentsCommand scheduledSync(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, null, false, false, true);
    }

    /** admin on-demand sync (단일 파일): enabled 여부와 무관, dispatch dedup, cursor 비변경. */
    public static SummarizeFigmaCommentsCommand singleFileSync(Long watchedFileId, Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, watchedFileId, false, false, false);
    }

    /** admin digest 진입점: dispatch 무시하고 재발송 가능, cursor 비변경. */
    public static SummarizeFigmaCommentsCommand digest(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, null, false, true, false);
    }

    /** admin preview 진입점 (전체 활성 파일): 발송/기록/cursor 모두 비변경. */
    public static SummarizeFigmaCommentsCommand preview(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, null, true, false, false);
    }

    /** admin preview 진입점 (단일 파일): enabled 여부 무관, 발송/기록/cursor 비변경. */
    public static SummarizeFigmaCommentsCommand previewSingleFile(Long watchedFileId, Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, watchedFileId, true, false, false);
    }
}
