package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.adapter.in.web.dto.request.AddFigmaRoutingMentionRequest;
import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaRoutingDomainRequest;
import com.umc.product.figma.adapter.in.web.dto.response.AddFigmaRoutingMentionResponse;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaRoutingDomainMentionResponse;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaRoutingDomainResponse;
import com.umc.product.figma.adapter.in.web.dto.response.RegisterFigmaRoutingDomainResponse;
import com.umc.product.figma.application.port.in.GetFigmaRoutingDomainUseCase;
import com.umc.product.figma.application.port.in.ManageFigmaRoutingDomainUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@Tag(name = "Figma Routing Domain | LLM 분류 라우팅 관리", description = "도메인 키 등록 + 담당자 mention 등록 / 조회")
public class FigmaRoutingDomainController {

    private final ManageFigmaRoutingDomainUseCase manageFigmaRoutingDomainUseCase;
    private final GetFigmaRoutingDomainUseCase getFigmaRoutingDomainUseCase;

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

    @Operation(summary = "[FIGMA-018] 라우팅 도메인 목록 조회 (mention 본문 미포함)")
    @GetMapping
    public List<FigmaRoutingDomainResponse> listDomains() {
        return getFigmaRoutingDomainUseCase.listDomains().stream()
            .map(FigmaRoutingDomainResponse::from)
            .toList();
    }

    @Operation(summary = "[FIGMA-019] 라우팅 도메인 단건 조회 (mention 포함)")
    @GetMapping("/{domainId}")
    public FigmaRoutingDomainResponse getDomain(@PathVariable Long domainId) {
        return FigmaRoutingDomainResponse.from(getFigmaRoutingDomainUseCase.getDomainById(domainId));
    }

    @Operation(summary = "[FIGMA-020] 라우팅 도메인의 담당자 mention 목록 조회")
    @GetMapping("/{domainId}/mentions")
    public List<FigmaRoutingDomainMentionResponse> listMentions(@PathVariable Long domainId) {
        return getFigmaRoutingDomainUseCase.listMentionsByDomainId(domainId).stream()
            .map(FigmaRoutingDomainMentionResponse::from)
            .toList();
    }
}
