package com.umc.product.figma.adapter.in.scheduler;

import com.umc.product.figma.adapter.out.external.FigmaSummaryProperties;
import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.out.SaveFigmaCommentDispatchPort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * figma_comment_dispatch 의 보존 기간 초과 행을 정리하는 회수 잡 (ADR-004 §Implementation Plan §7).
 * <p>
 * dispatch 테이블은 "이미 발송된 댓글" 의 가드 책임만 가지므로, 운영적으로 의미를 잃은 (보존 기간을 넘긴) 행을 주기적으로 삭제해 무한 누적을 막는다. 회수 주기는
 * {@code app.figma.summary.retention-poll-interval} (기본 24시간) 으로, 보존 기간은 {@code app.figma.summary.dispatch-retention}
 * (기본 90일) 로 설정한다.
 * <p>
 * 운영진이 이전 시간창을 명시적으로 force=true 로 재발송하려면, dispatch 가 존재하던 댓글이 회수된 시점부터는 force 옵션 없이도 재발송된다 (의도된 동작 — 보존 기간을 넘은 댓글은 더 이상
 * 신뢰할 dispatch 기록이 없다).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentDispatchRetentionScheduler {

    private final SaveFigmaCommentDispatchPort saveFigmaCommentDispatchPort;
    private final FigmaSyncProperties figmaSyncProperties;
    private final FigmaSummaryProperties figmaSummaryProperties;

    @Scheduled(fixedDelayString = "${app.figma.summary.retention-poll-interval}")
    public void purge() {
        if (!figmaSyncProperties.enabled()) {
            return;
        }
        Instant threshold = Instant.now().minus(figmaSummaryProperties.dispatchRetention());
        int deleted = saveFigmaCommentDispatchPort.deleteOlderThan(threshold);
        if (deleted > 0) {
            log.info("figma_comment_dispatch 회수 완료: threshold={}, deleted={}", threshold, deleted);
        } else {
            log.debug("figma_comment_dispatch 회수: 보존 기간 초과 행 없음. threshold={}", threshold);
        }
    }
}
