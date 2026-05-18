package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일별 fetch 모니터링 상태 (last_synced_at, last_error) 를 별도 트랜잭션으로 갱신한다 (ADR-004 §Decision 5 이후).
 * <p>
 * 별도 빈으로 분리한 이유는 Spring AOP self-invocation 한계를 피하기 위함이며, batch 처리 중 한 파일의 갱신 실패가 다른 파일의 갱신에 영향을 주지 않게 하기 위함이다.
 */
@Component
@RequiredArgsConstructor
public class FigmaWatchedFileStateUpdater {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final SaveFigmaWatchedFilePort saveFigmaWatchedFilePort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordError(Long fileId, String message) {
        loadFigmaWatchedFilePort.findById(fileId).ifPresent(file -> {
            file.recordError(message);
            saveFigmaWatchedFilePort.save(file);
        });
    }

    /**
     * fetch 가 정상 완료된 (또는 신규 댓글이 0건이었던) 파일의 last_synced_at 만 현재 시각으로 갱신한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markIdle(Long fileId) {
        loadFigmaWatchedFilePort.findById(fileId).ifPresent(file -> {
            file.markFetched(Instant.now());
            saveFigmaWatchedFilePort.save(file);
        });
    }
}
