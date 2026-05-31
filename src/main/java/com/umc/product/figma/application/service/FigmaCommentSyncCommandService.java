package com.umc.product.figma.application.service;

import com.umc.product.figma.config.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import com.umc.product.figma.application.port.out.LoadFigmaSummaryCursorPort;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 운영자 수동 트리거 sync 진입점의 thin shim. 스케줄러와 동일하게 figma_summary_cursor 기반 시간창으로 SummarizeFigmaCommentsUseCase 를 호출한다
 * (ADR-004 §Decision 1·2).
 * <p>
 * syncOne 은 cursor 와 무관한 단일 파일 trace 모드로 동작하며, dispatch dedup 은 적용하되 cursor advance 는 하지 않는다 — 단일 파일 디버깅이 전역 cursor 를
 * 흔들지 않게 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentSyncCommandService implements SyncFigmaCommentsUseCase {

    private static final long BOOTSTRAP_INTERVAL_MULTIPLIER = 2L;

    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final LoadFigmaSummaryCursorPort loadFigmaSummaryCursorPort;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public void syncAll() {
        Instant now = Instant.now();
        Duration interval = figmaSyncProperties.pollInterval();
        Instant from = loadFigmaSummaryCursorPort.findCursor()
            .map(c -> c.getLastWindowEnd())
            .orElseGet(() -> now.minus(interval.multipliedBy(BOOTSTRAP_INTERVAL_MULTIPLIER)));
        summarizeFigmaCommentsUseCase.summarize(SummarizeFigmaCommentsCommand.scheduledSync(from, now));
    }

    @Override
    public void syncOne(Long watchedFileId) {
        Instant now = Instant.now();
        Duration interval = figmaSyncProperties.pollInterval();
        Instant from = now.minus(interval.multipliedBy(BOOTSTRAP_INTERVAL_MULTIPLIER));
        summarizeFigmaCommentsUseCase.summarize(
            SummarizeFigmaCommentsCommand.singleFileSync(watchedFileId, from, now)
        );
    }
}
