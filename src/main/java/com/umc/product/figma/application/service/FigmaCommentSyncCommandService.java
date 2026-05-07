package com.umc.product.figma.application.service;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 정기 sync / on-demand sync 진입점의 thin shim. 시간창 단일 본체
 * ({@link SummarizeFigmaCommentsUseCase}) 로 위임한다 (ADR-004 §Decision 1·2).
 * <p>
 * 본 커밋 시점에는 cursor 가 아직 스케줄러에서 통합되지 않았으므로 syncAll 의 시간창은 안전 fallback 인 (now - pollInterval × 2, now] 로 정한다. 다음 커밋 (ADR §5) 에서
 * 스케줄러가 cursor 를 직접 읽어 호출 시점에 시간창을 결정하면, 본 service 는 admin 트리거 전용 경로로 남는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentSyncCommandService implements SyncFigmaCommentsUseCase {

    private static final long BOOTSTRAP_INTERVAL_MULTIPLIER = 2L;

    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public void syncAll() {
        Instant now = Instant.now();
        Duration interval = figmaSyncProperties.pollInterval();
        Instant from = now.minus(interval.multipliedBy(BOOTSTRAP_INTERVAL_MULTIPLIER));
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
