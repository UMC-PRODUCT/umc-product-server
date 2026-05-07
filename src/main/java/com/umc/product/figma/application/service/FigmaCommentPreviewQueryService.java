package com.umc.product.figma.application.service;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.PreviewFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;
import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 단일 watched file 의 preview 진입점의 thin shim (ADR-004 §Decision 2).
 * <p>
 * 시간창 단일 본체 ({@link SummarizeFigmaCommentsUseCase}) 로 위임하며, dryRun=true 로 발송 / dispatch / cursor 를 모두 비변경 처리한다. 시간창 기본값은 (now -
 * pollInterval × 2, now] 이다 — 정기 sync 가 한 번 누락되어 직전 cursor 가 약간 오래된 상황에서도 같은 댓글이 보이도록 의도한 것이다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentPreviewQueryService implements PreviewFigmaCommentsUseCase {

    private static final long PREVIEW_INTERVAL_MULTIPLIER = 2L;

    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public FigmaCommentPreviewInfo preview(Long watchedFileId) {
        FigmaWatchedFile watchedFile = loadFigmaWatchedFilePort.findById(watchedFileId)
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.WATCHED_FILE_NOT_FOUND));

        Instant now = Instant.now();
        Duration interval = figmaSyncProperties.pollInterval();
        Instant from = now.minus(interval.multipliedBy(PREVIEW_INTERVAL_MULTIPLIER));

        FigmaSummaryResult result = summarizeFigmaCommentsUseCase.summarize(
            SummarizeFigmaCommentsCommand.previewSingleFile(watchedFileId, from, now)
        );

        List<FigmaCommentPreviewInfo.DomainGroup> domainGroups = result.domains().stream()
            .map(d -> new FigmaCommentPreviewInfo.DomainGroup(
                d.domainKey(),
                d.webhookUrl(),
                d.fallback(),
                d.mentionRenders(),
                d.comments().stream()
                    .map(c -> new FigmaCommentPreviewInfo.Comment(
                        c.commentId(),
                        c.message(),
                        c.authorName(),
                        c.nodeId(),
                        c.pageName(),
                        c.classifiedDomainKey(),
                        c.createdAt()
                    ))
                    .toList()
            ))
            .toList();

        return new FigmaCommentPreviewInfo(
            watchedFile.getFileKey(),
            watchedFile.getDisplayName(),
            watchedFile.getLastSyncedCommentId(),
            watchedFile.getLastSyncedAt(),
            result.totalComments(),
            result.unmatchedCount(),
            domainGroups
        );
    }
}
