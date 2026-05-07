package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.adapter.out.external.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 시간창 기반 figma 댓글 preview 의 generic admin endpoint (ADR-004 §Implementation Plan §6).
 * <p>
 * Discord 발송 / dispatch 기록 / cursor 갱신을 모두 건너뛰고, [from, to] 시간창 안의 댓글이 어떤 도메인으로 묶여 갈지를 응답한다. 응답의 각 댓글에는
 * {@link FigmaSummaryResult.Comment#alreadyDispatched()} 가 포함되어 운영진이 "이 중 무엇이 다음 sync 에서 실제로 발송될지" 를 사전 검증할 수 있다.
 * <p>
 * 단일 파일 한정 preview 가 필요하면 {@code watchedFileId} 쿼리를 함께 넘긴다 — 해당 파일의 enabled 여부와 무관하게 동작한다. ADR-007 에 따라 SUPER_ADMIN 만
 * 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/preview")
@RequiredArgsConstructor
@Tag(name = "Figma | 댓글 분류하기", description = "[from, to] 안의 댓글 묶음 결과를 발송 없이 조회")
public class FigmaPreviewController {

    private static final long DEFAULT_INTERVAL_MULTIPLIER = 2L;

    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final FigmaSyncProperties figmaSyncProperties;

    @Operation(summary = "[FIGMA-010] 시간창 preview (Discord 발송 X, dispatch / cursor 비변경)")
    @GetMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public FigmaSummaryResult preview(
        @RequestParam(value = "from", required = false) Instant from,
        @RequestParam(value = "to", required = false) Instant to,
        @RequestParam(value = "watchedFileId", required = false) Long watchedFileId
    ) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null
            ? from
            : resolvedTo.minus(figmaSyncProperties.pollInterval().multipliedBy(DEFAULT_INTERVAL_MULTIPLIER));

        SummarizeFigmaCommentsCommand command = watchedFileId != null
            ? SummarizeFigmaCommentsCommand.previewSingleFile(watchedFileId, resolvedFrom, resolvedTo)
            : SummarizeFigmaCommentsCommand.preview(resolvedFrom, resolvedTo);

        return summarizeFigmaCommentsUseCase.summarize(command);
    }
}
