package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.application.port.out.SaveFigmaWatchedFilePort;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 파일별 sync 상태(last_synced_comment_id, last_error) 를 별도 트랜잭션으로 갱신한다.
 * <p>
 * 별도 빈으로 분리한 이유는 Spring AOP self-invocation 한계를 피하기 위함이며, batch 처리 중 한 파일의 갱신 실패가 다른 파일의 갱신에 영향을 주지 않게 하기 위함이다.
 */
@Component
@RequiredArgsConstructor
public class FigmaWatchedFileStateUpdater {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final SaveFigmaWatchedFilePort saveFigmaWatchedFilePort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void advance(Long fileId, String latestCommentId) {
        loadFigmaWatchedFilePort.findById(fileId).ifPresent(file -> {
            file.markSynced(latestCommentId, Instant.now());
            saveFigmaWatchedFilePort.save(file);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordError(Long fileId, String message) {
        loadFigmaWatchedFilePort.findById(fileId).ifPresent(file -> {
            file.recordError(message);
            saveFigmaWatchedFilePort.save(file);
        });
    }

    /**
     * 신규 댓글이 0건인 경우에도 last_synced_at 만 갱신하기 위해 사용된다 (last_error 도 함께 비워짐).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markIdle(Long fileId) {
        loadFigmaWatchedFilePort.findById(fileId).ifPresent(file -> {
            file.markSynced(file.getLastSyncedCommentId(), Instant.now());
            saveFigmaWatchedFilePort.save(file);
        });
    }
}
