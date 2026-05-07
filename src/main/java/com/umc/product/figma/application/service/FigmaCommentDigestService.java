package com.umc.product.figma.application.service;

import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.DigestFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;
import com.umc.product.figma.application.port.out.LoadFigmaWatchedFilePort;
import com.umc.product.figma.domain.FigmaWatchedFile;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 운영진이 명시적으로 지정한 [from, to] 시간창에 속한 댓글을 도메인별로 묶어 Discord 로 발송한다.
 * sync 상태(last_synced_comment_id, last_synced_at, last_error) 는 변경하지 않으므로
 * 정기 폴링과 독립적으로 호출할 수 있다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentDigestService implements DigestFigmaCommentsUseCase {

    private final LoadFigmaWatchedFilePort loadFigmaWatchedFilePort;
    private final FigmaIntegrationCommandService figmaIntegrationCommandService;
    private final FigmaCommentBatchProcessor figmaCommentBatchProcessor;
    private final FigmaSyncProperties figmaSyncProperties;

    @Override
    public FigmaDigestSummary digest(DigestFigmaCommentsCommand command) {
        if (command.from() == null || command.to() == null) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "from / to 둘 다 필수입니다.");
        }
        if (command.from().isAfter(command.to())) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "from 이 to 보다 이후일 수 없습니다.");
        }

        List<FigmaWatchedFile> files = loadFigmaWatchedFilePort.listEnabled(figmaSyncProperties.maxFilesPerRun());
        if (files.isEmpty()) {
            return new FigmaDigestSummary(command.from(), command.to(), 0, 0, List.of());
        }

        String accessToken = figmaIntegrationCommandService.resolveActiveAccessToken();

        FigmaCommentBatchProcessor.BatchSummary batch = figmaCommentBatchProcessor.processDigestWindow(
            files, accessToken, command.from(), command.to()
        );

        List<FigmaDigestSummary.DomainResult> domainResults = batch.sendResults().stream()
            .map(r -> new FigmaDigestSummary.DomainResult(r.domainKey(), r.commentCount(), r.sent()))
            .toList();

        log.info("Figma digest 완료: from={}, to={}, total={}, unmatched={}, domains={}",
            command.from(), command.to(), batch.totalComments(), batch.unmatchedCount(), domainResults.size());

        return new FigmaDigestSummary(
            command.from(),
            command.to(),
            batch.totalComments(),
            batch.unmatchedCount(),
            domainResults
        );
    }
}
