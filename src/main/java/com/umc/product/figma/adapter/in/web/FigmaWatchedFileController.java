package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaWatchedFileRequest;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaWatchedFileResponse;
import com.umc.product.figma.adapter.in.web.dto.response.RegisterFigmaWatchedFileResponse;
import com.umc.product.figma.application.port.in.GetFigmaWatchedFileUseCase;
import com.umc.product.figma.application.port.in.ManageFigmaWatchedFileUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Figma 폴링 대상 파일 관리. 모든 endpoint 는 전역 관리자만 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/watched-files")
@RequiredArgsConstructor
@Tag(name = "Figma | 대상 파일 관리", description = "업데이트 대상 Figma 파일 관리")
public class FigmaWatchedFileController {

    private final ManageFigmaWatchedFileUseCase manageFigmaWatchedFileUseCase;
    private final GetFigmaWatchedFileUseCase getFigmaWatchedFileUseCase;

    @Operation(summary = "[FIGMA-003] 폴링 대상 파일 등록")
    @PostMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public RegisterFigmaWatchedFileResponse register(
        @RequestBody @Valid RegisterFigmaWatchedFileRequest request
    ) {
        Long id = manageFigmaWatchedFileUseCase.register(request.toCommand());
        return new RegisterFigmaWatchedFileResponse(id);
    }

    @Operation(summary = "[FIGMA-004] 폴링 대상 파일 비활성화")
    @DeleteMapping("/{watchedFileId}")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void disable(@PathVariable Long watchedFileId) {
        manageFigmaWatchedFileUseCase.disable(watchedFileId);
    }

    @Operation(summary = "[FIGMA-005] 폴링 대상 파일 활성화")
    @PostMapping("/{watchedFileId}/enable")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void enable(@PathVariable Long watchedFileId) {
        manageFigmaWatchedFileUseCase.enable(watchedFileId);
    }

    @Operation(summary = "[FIGMA-008] 폴링 대상 파일 목록 조회 (enabled 필터)")
    @GetMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public List<FigmaWatchedFileResponse> listFiles(
        @RequestParam(required = false) Boolean enabled
    ) {
        return getFigmaWatchedFileUseCase.listAll(enabled).stream()
            .map(FigmaWatchedFileResponse::from)
            .toList();
    }

    @Operation(summary = "[FIGMA-009] 폴링 대상 파일 단건 조회 (sync 상태 포함)")
    @GetMapping("/{watchedFileId}")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public FigmaWatchedFileResponse getFile(@PathVariable Long watchedFileId) {
        return FigmaWatchedFileResponse.from(getFigmaWatchedFileUseCase.getById(watchedFileId));
    }
}
