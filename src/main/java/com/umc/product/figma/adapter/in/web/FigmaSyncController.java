package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.application.port.in.PreviewFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import com.umc.product.figma.application.port.in.dto.FigmaCommentPreviewInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영진이 스케줄러 주기를 기다리지 않고 Figma 댓글 동기화를 즉시 트리거하거나, Discord 발송 없이 신규 댓글 / 매칭될 라우트만 미리 확인할 수 있는 admin API.
 * <p>
 * ADR-007 에 따라 모든 endpoint 는 SUPER_ADMIN 만 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/sync")
@RequiredArgsConstructor
@Tag(name = "Figma Sync | 수동 트리거", description = "Figma 댓글 동기화 on-demand 실행 / preview")
public class FigmaSyncController {

    private final SyncFigmaCommentsUseCase syncFigmaCommentsUseCase;
    private final PreviewFigmaCommentsUseCase previewFigmaCommentsUseCase;

    @Operation(summary = "[FIGMA-006] 활성 파일 전체 즉시 동기화")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void syncAll() {
        syncFigmaCommentsUseCase.syncAll();
    }

    @Operation(summary = "[FIGMA-007] 특정 파일 즉시 동기화 (enabled 무관)")
    @PostMapping("/watched-files/{watchedFileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void syncOne(@PathVariable Long watchedFileId) {
        syncFigmaCommentsUseCase.syncOne(watchedFileId);
    }

    /**
     * @deprecated 시간창 시맨틱의 generic preview {@link FigmaPreviewController} 로 대체된다 (ADR-004 §Implementation Plan §6). 본 endpoint 는 단일 파일
     * preview 호환을 위해 한시 유지된다.
     */
    @Deprecated
    @Operation(summary = "[FIGMA-010] (deprecated) 특정 파일의 최근 시간창 댓글 미리보기 — /api/v1/admin/figma/preview 사용 권장")
    @GetMapping("/watched-files/{watchedFileId}/preview")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public FigmaCommentPreviewInfo preview(@PathVariable Long watchedFileId) {
        return previewFigmaCommentsUseCase.preview(watchedFileId);
    }
}
