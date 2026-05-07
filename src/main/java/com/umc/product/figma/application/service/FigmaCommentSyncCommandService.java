package com.umc.product.figma.application.service;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Figma 폴링 → Discord 포워딩 메인 유즈케이스의 오케스트레이션 계층.
 *
 * 한 사이클의 모든 활성 파일 신규 댓글을 모아 LLM 분류기로 분류한 뒤 도메인별로 묶어
 * Discord 메시지 1건(필요 시 페이지 분할) 으로 발송한다. 시간창은 파일별 last_synced_comment_id
 * 이후의 모든 신규 댓글로 정의되며, 발송 후 last_synced_comment_id 가 갱신된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentSyncCommandService implements SyncFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;
    private final FigmaCommentBatchProcessor figmaCommentBatchProcessor;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public void syncAll() {
        List<FigmaWatchedFile> files = loadFigmaWatchedFilePort.listEnabled(figmaSyncProperties.maxFilesPerRun());
        runSync(files, "syncAll");
    }

    @Override
    public void syncOne(Long watchedFileId) {
        FigmaWatchedFile file = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));
        runSync(List.of(file), "syncOne(" + watchedFileId + ")");
    }

    private void runSync(List<FigmaWatchedFile> files, String invocation) {
        if (files.isEmpty()) {
            return;
        }

        String accessToken;
        try {
            accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();
        } catch (FigmaDomainException e) {
            log.warn("Figma access token 확보 실패. {} 동기화를 건너뜁니다: {}", invocation, e.getMessage());
            return;
        }

        try {
            FigmaCommentBatchProcessor.BatchSummary summary =
                figmaCommentBatchProcessor.processSyncCycle(files, accessToken);
            log.debug("Figma {} 완료: total={}, unmatched={}, sentDomains={}",
                invocation, summary.totalComments(), summary.unmatchedCount(), summary.sendResults().size());
        } catch (FigmaDomainException e) {
            log.warn("Figma {} 실패: {}", invocation, e.getMessage());
        }
    }
}
