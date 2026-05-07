package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.application.port.in.SyncFigmaCommentsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운영진이 스케줄러 주기를 기다리지 않고 Figma 댓글 동기화를 즉시 트리거하는 admin API.
 * <p>
 * preview 는 {@link FigmaPreviewController} 의 시간창 generic endpoint 로 분리됐다 (ADR-004 §Implementation Plan §6).
 * <p>
 * ADR-007 에 따라 모든 endpoint 는 SUPER_ADMIN 만 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/sync")
@RequiredArgsConstructor
@Tag(name = "Figma | 수동 트리거", description = "Figma 댓글 동기화 on-demand 실행")
public class FigmaSyncController {

    private final SyncFigmaCommentsUseCase syncFigmaCommentsUseCase;

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
}
