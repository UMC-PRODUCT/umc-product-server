package com.umc.product.figma.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.umc.product.figma.application.port.in.DigestFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;
import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 운영진의 catch-up digest 진입점의 thin shim (ADR-004 §Decision 2).
 * <p>
 * 시간창 단일 본체 ({@link SummarizeFigmaCommentsUseCase}) 로 위임하며, force=true 로 dispatch 행이 있는 댓글도 재발송한다. cursor 는 변경하지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FigmaCommentDigestService implements DigestFigmaCommentsUseCase {

    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;

    @Override
    public FigmaDigestSummary digest(DigestFigmaCommentsCommand command) {
        if (command.from() == null || command.to() == null) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "요약할 시작 시간과 종료 시간을 모두 입력해주세요.");
        }
        if (command.from().isAfter(command.to())) {
            throw new FigmaDomainException(FigmaErrorCode.DIGEST_RANGE_INVALID, "요약 시작 시간은 종료 시간보다 빨라야 해요. 기간을 다시 선택해주세요.");
        }

        FigmaSummaryResult result = summarizeFigmaCommentsUseCase.summarize(
            SummarizeFigmaCommentsCommand.digest(command.from(), command.to())
        );

        List<FigmaDigestSummary.DomainResult> domainResults = result.domains().stream()
            .map(d -> new FigmaDigestSummary.DomainResult(d.domainKey(), d.comments().size(), d.sent()))
            .toList();

        log.info("Figma digest를 생성했습니다: from={}, to={}, total={}, unmatched={}, skippedDispatched={}, domains={}",
            result.from(), result.to(), result.totalComments(), result.unmatchedCount(),
            result.skippedAlreadyDispatchedCount(), domainResults.size());

        return new FigmaDigestSummary(
            result.from(),
            result.to(),
            result.totalComments(),
            result.unmatchedCount(),
            domainResults
        );
    }
}
