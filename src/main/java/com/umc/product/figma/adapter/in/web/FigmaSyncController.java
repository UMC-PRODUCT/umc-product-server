package com.umc.product.figma.adapter.in.web;

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
 * 운영진이 스케줄러 주기를 기다리지 않고 Figma 댓글 동기화를 즉시 트리거할 수 있는 admin API.
 * 내부적으로 스케줄러와 동일한 {@link SyncFigmaCommentsUseCase} 를 호출한다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/sync")
@RequiredArgsConstructor
@Tag(name = "Figma Sync | 수동 트리거", description = "Figma 댓글 동기화를 on-demand 로 실행")
public class FigmaSyncController {

    private final SyncFigmaCommentsUseCase syncFigmaCommentsUseCase;

    @Operation(summary = "[FIGMA-006] 활성 파일 전체 즉시 동기화")
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncAll() {
        syncFigmaCommentsUseCase.syncAll();
    }

    @Operation(summary = "[FIGMA-007] 특정 파일 즉시 동기화 (enabled 무관)")
    @PostMapping("/watched-files/{watchedFileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void syncOne(@PathVariable Long watchedFileId) {
        syncFigmaCommentsUseCase.syncOne(watchedFileId);
    }
}
