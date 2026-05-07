package com.umc.product.figma.adapter.in.scheduler;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentSyncScheduler {

    private final FigmaSyncProperties figmaSyncProperties;
    private final SyncFigmaCommentsUseCase syncFigmaCommentsUseCase;

    @Scheduled(fixedDelayString = "${app.figma.sync.poll-interval}")
    public void poll() {
        if (!figmaSyncProperties.enabled()) {
            return;
        }
        log.debug("Figma 댓글 폴링 스케줄 실행");
        syncFigmaCommentsUseCase.syncAll();
    }
}
