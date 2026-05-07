package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.adapter.in.web.dto.request.AddFigmaRoutingMentionRequest;
import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaRoutingDomainRequest;
import com.umc.product.figma.adapter.in.web.dto.response.AddFigmaRoutingMentionResponse;
import com.umc.product.figma.adapter.in.web.dto.response.RegisterFigmaRoutingDomainResponse;
import com.umc.product.figma.application.port.in.ManageFigmaRoutingDomainUseCase;
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
 * LLM 분류 결과(domain_key) 단위 라우팅 도메인 + 담당자 mention 관리. 운영진이 화면에서 도메인을 등록하고, 도메인별로 담당자 (Discord role/user) 를 추가/제거한다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/routing-domains")
@RequiredArgsConstructor
@Tag(name = "Figma Routing Domain | LLM 분류 라우팅 관리", description = "도메인 키 등록 + 담당자 mention 등록")
public class FigmaRoutingDomainController {

    private final ManageFigmaRoutingDomainUseCase manageFigmaRoutingDomainUseCase;

    @Operation(summary = "[FIGMA-011] 라우팅 도메인 등록")
    @PostMapping
    public RegisterFigmaRoutingDomainResponse registerDomain(
        @RequestBody @Valid RegisterFigmaRoutingDomainRequest request
    ) {
        Long id = manageFigmaRoutingDomainUseCase.registerDomain(request.toCommand());
        return new RegisterFigmaRoutingDomainResponse(id);
    }

    @Operation(summary = "[FIGMA-012] 라우팅 도메인 삭제 (mention 도 cascade)")
    @DeleteMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDomain(@PathVariable Long domainId) {
        manageFigmaRoutingDomainUseCase.deleteDomain(domainId);
    }

    @Operation(summary = "[FIGMA-013] 라우팅 도메인에 담당자 mention 추가")
    @PostMapping("/{domainId}/mentions")
    public AddFigmaRoutingMentionResponse addMention(
        @PathVariable Long domainId,
        @RequestBody @Valid AddFigmaRoutingMentionRequest request
    ) {
        Long id = manageFigmaRoutingDomainUseCase.addMention(request.toCommand(domainId));
        return new AddFigmaRoutingMentionResponse(id);
    }

    @Operation(summary = "[FIGMA-014] 담당자 mention 삭제")
    @DeleteMapping("/mentions/{mentionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMention(@PathVariable Long mentionId) {
        manageFigmaRoutingDomainUseCase.removeMention(mentionId);
    }
}
