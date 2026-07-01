package com.umc.product.figma.adapter.in.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import com.umc.product.figma.application.port.out.LoadFigmaSummaryCursorPort;
import com.umc.product.figma.config.FigmaSyncProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시간창 기반 figma 댓글 요약/발송의 정기 진입점 (ADR-004 §Decision 4 / §Implementation Plan §5).
 * <p>
 * 매 사이클 figma_summary_cursor 의 last_window_end 를 from 으로, 현재 시각을 to 로 하는 시간창으로 SummarizeFigmaCommentsUseCase 를 호출한다.
 * cursor 가 부재한 부트스트랩 환경에서는 안전 fallback 으로 (now - pollInterval × 2, now] 를 사용한다 — 첫 사이클이 직전 짧은 구간만 보더라도 dispatch dedup
 * 으로 중복이 막힌다.
 * <p>
 * 다중 인스턴스 환경에서 두 인스턴스가 동시에 트리거되면 둘 다 같은 시간창을 처리하지만, dispatch unique(comment_id) 제약과 cursor 의 방어적 advance 로 중복 발송이 차단된다
 * (LLM/Figma REST 호출이 도중에 잠시 중복될 수 있다는 trade-off 가 남는다 — ShedLock 도입은 후속 작업).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.figma.sync.enabled", havingValue = "true")
public class FigmaCommentSyncScheduler {

    private static final long BOOTSTRAP_INTERVAL_MULTIPLIER = 2L;

    private final FigmaSyncProperties figmaSyncProperties;
    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final LoadFigmaSummaryCursorPort loadFigmaSummaryCursorPort;

    @Scheduled(fixedDelayString = "${app.figma.sync.poll-interval}")
    public void poll() {
        Instant now = Instant.now();
        Duration interval = figmaSyncProperties.pollInterval();
        Instant from = loadFigmaSummaryCursorPort.findCursor()
            .map(c -> c.getLastWindowEnd())
            .orElseGet(() -> now.minus(interval.multipliedBy(BOOTSTRAP_INTERVAL_MULTIPLIER)));
        log.debug("Figma 시간창 폴링: from={}, to={}", from, now);
        summarizeFigmaCommentsUseCase.summarize(SummarizeFigmaCommentsCommand.scheduledSync(from, now));
    }
}
