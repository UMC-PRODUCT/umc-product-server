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
 * 파일 단위 트랜잭션은 {@link FigmaSingleFileSyncProcessor}에 위임한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentSyncCommandService implements SyncFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;
    private final FigmaSingleFileSyncProcessor figmaSingleFileSyncProcessor;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public void syncAll() {
        List<FigmaWatchedFile> watchedFiles = loadFigmaWatchedFilePort.listEnabled(figmaSyncProperties.maxFilesPerRun());
        if (watchedFiles.isEmpty()) {
            return;
        }

        String accessToken;
        try {
            accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();
        } catch (FigmaDomainException e) {
            log.warn("Figma access token 확보 실패. 동기화를 건너뜁니다: {}", e.getMessage());
            return;
        }

        for (FigmaWatchedFile watchedFile : watchedFiles) {
            try {
                figmaSingleFileSyncProcessor.process(watchedFile.getId(), accessToken);
            } catch (RuntimeException e) {
                log.error("Figma 파일 동기화 중 예기치 못한 예외: fileKey={}",
                    watchedFile.getFileKey(), e);
            }
        }
    }

    @Override
    public void syncOne(Long watchedFileId) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));

        String accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();
        figmaSingleFileSyncProcessor.process(watchedFile.getId(), accessToken);
    }
}
