package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaPartRouteRequest;
import com.umc.product.figma.adapter.in.web.dto.response.RegisterFigmaPartRouteResponse;
import com.umc.product.figma.application.port.in.ManageFigmaPartRouteUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 페이지 이름 → 파트 role + Discord webhook 매핑 관리.
 * Discord webhook URL 은 라우트 단위로 행에 보관되며, 본 API 로 등록/삭제한다.
 * fallback=true 인 라우트는 매핑되지 않은 댓글이 도달할 기본 채널이며,
 * 관용적으로 page_name 에 "*" 같은 placeholder 를 사용한다 (UNIQUE 제약상 파일당 하나).
 */
@RestController
@RequestMapping("/api/v1/admin/figma/part-routes")
@RequiredArgsConstructor
@Tag(name = "Figma Part Route | 파트 라우팅 관리", description = "페이지명 → 담당 파트 + Discord 채널/role 매핑")
public class FigmaPartRouteController {

    private final ManageFigmaPartRouteUseCase manageFigmaPartRouteUseCase;

    @Operation(summary = "[FIGMA-008] 파트 라우트 등록")
    @PostMapping
    public RegisterFigmaPartRouteResponse register(
        @RequestBody @Valid RegisterFigmaPartRouteRequest request
    ) {
        Long id = manageFigmaPartRouteUseCase.register(request.toCommand());
        return new RegisterFigmaPartRouteResponse(id);
    }

    @Operation(summary = "[FIGMA-009] 파트 라우트 삭제")
    @DeleteMapping("/{routeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long routeId) {
        manageFigmaPartRouteUseCase.delete(routeId);
    }
}
