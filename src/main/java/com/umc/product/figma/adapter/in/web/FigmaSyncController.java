package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.config.FigmaSyncProperties;
import com.umc.product.figma.application.port.in.DigestFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SummarizeFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.DigestFigmaCommentsCommand;
import com.umc.product.figma.application.port.in.dto.FigmaDigestSummary;
import com.umc.product.figma.application.port.in.dto.FigmaSummaryResult;
import com.umc.product.figma.application.port.in.dto.SummarizeFigmaCommentsCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Figma 댓글 동기화 admin API 통합 컨트롤러.
 * <p>
 * 셋 모두 시간창 안의 댓글을 다루지만 부수효과의 강도가 다르다.
 * <ul>
 *   <li>POST /sync, POST /sync/watched-files/{id}
 *       — 정상 흐름 즉시 실행. Discord 발송 O, cursor 갱신 O, dispatch 기록 O. 멱등.</li>
 *   <li>POST /digest — 시간창 강제 발송. Discord 발송 O, cursor 갱신 X, dispatch 무시. 비-멱등.</li>
 *   <li>GET /preview — 분류 결과 조회만. 발송 X, 상태 변경 X.</li>
 * </ul>
 * ADR-007 에 따라 모든 endpoint 는 SUPER_ADMIN 만 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma")
@RequiredArgsConstructor
@Tag(name = "Figma | Comment 동기화 및 조회", description = "Figma 댓글 동기화, 요약, 미리보기를 다룹니다.")
public class FigmaSyncController {

    private static final long DEFAULT_INTERVAL_MULTIPLIER = 2L;

    private final SyncFigmaCommentsUseCase syncFigmaCommentsUseCase;
    private final DigestFigmaCommentsUseCase digestFigmaCommentsUseCase;
    private final SummarizeFigmaCommentsUseCase summarizeFigmaCommentsUseCase;
    private final FigmaSyncProperties figmaSyncProperties;

    @Operation(
        operationId = "FIGMA-006",
        summary = "활성 파일 전체 즉시 동기화",
        description = """
            정기 폴링 스케줄러가 도는 동기화 로직을 즉시 한 번 실행한다.
            마지막 cursor 이후의 새 댓글만 Discord 로 보내고, cursor 를 전진시키며,
            dispatch 기록을 남긴다. 같은 호출을 두 번 해도 같은 댓글이 두 번 발송되지 않는다(멱등).
            스케줄러가 늦거나 즉시 반영이 필요할 때 사용한다.
            """
    )
    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void syncAll() {
        syncFigmaCommentsUseCase.syncAll();
    }

    @Operation(
        operationId = "FIGMA-007",
        summary = "특정 파일 즉시 동기화",
        description = """
            지정한 watchedFileId 한 개만 즉시 동기화한다. 해당 파일의 enabled 여부와 무관하게 동작한다.
            sync 전체와 동일하게 Discord 발송, cursor 갱신, dispatch 기록을 모두 수행하며 멱등하다.
            """
    )
    @PostMapping("/sync/watched-files/{watchedFileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void syncOne(@PathVariable Long watchedFileId) {
        syncFigmaCommentsUseCase.syncOne(watchedFileId);
    }

    @Operation(
        operationId = "FIGMA-015",
        summary = "특정 시간대 누락 댓글 동기화",
        description = """
            [from, to] 시간창의 댓글을 도메인별로 묶어 Discord 로 보내고 JSON 요약을 반환한다.
            cursor 를 갱신하지 않고 force=true 로 dispatch 기록도 무시하므로,
            동일 시간창을 여러 번 호출하면 그때마다 다시 발송된다(비-멱등).
            정기 sync 와 무관한 운영진의 catch-up / 회고용 도구.
            """
    )
    @PostMapping("/digest")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public FigmaDigestSummary digest(
        @RequestParam Instant from,
        @RequestParam Instant to
    ) {
        return digestFigmaCommentsUseCase.digest(new DigestFigmaCommentsCommand(from, to));
    }

    @Operation(
        operationId = "FIGMA-010",
        summary = "특정 시간대 미리보기 (Discord 발송 X, dispatch / cursor 비변경)",
        description = """
            [from, to] 시간창의 댓글이 어떤 도메인으로 묶여 갈지를 발송 없이 응답한다.
            Discord 발송, cursor 갱신, dispatch 기록을 모두 건너뛴다.
            응답의 각 댓글에는 alreadyDispatched 가 포함되어 운영진이
            "이 중 무엇이 다음 sync 에서 실제로 발송될지" 를 사전 검증할 수 있다.

            from / to 를 생략하면 기본값으로 (now - 2 * pollInterval) ~ now 시간창을 사용한다.
            watchedFileId 를 함께 넘기면 해당 파일 한정으로 동작하며, enabled 여부와 무관하다.
            """
    )
    @GetMapping("/preview")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public FigmaSummaryResult preview(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) Long watchedFileId
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
