package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaWatchedFileRequest;
import com.umc.product.figma.adapter.in.web.dto.response.RegisterFigmaWatchedFileResponse;
import com.umc.product.figma.application.port.in.ManageFigmaWatchedFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/figma/watched-files")
@RequiredArgsConstructor
@Tag(name = "Figma Watched File | 폴링 대상 파일 관리", description = "Figma 댓글을 Discord로 포워딩할 파일 등록/해제")
public class FigmaWatchedFileController {

    private final ManageFigmaWatchedFileUseCase manageFigmaWatchedFileUseCase;

    @Operation(summary = "[FIGMA-003] 폴링 대상 파일 등록")
    @PostMapping
    public RegisterFigmaWatchedFileResponse register(
        @RequestBody @Valid RegisterFigmaWatchedFileRequest request
    ) {
        Long id = manageFigmaWatchedFileUseCase.register(request.toCommand());
        return new RegisterFigmaWatchedFileResponse(id);
    }

    @Operation(summary = "[FIGMA-004] 폴링 대상 파일 비활성화")
    @DeleteMapping("/{watchedFileId}")
    public void disable(@PathVariable Long watchedFileId) {
        manageFigmaWatchedFileUseCase.disable(watchedFileId);
    }

    @Operation(summary = "[FIGMA-005] 폴링 대상 파일 활성화")
    @PostMapping("/{watchedFileId}/enable")
    public void enable(@PathVariable Long watchedFileId) {
        manageFigmaWatchedFileUseCase.enable(watchedFileId);
    }
}
