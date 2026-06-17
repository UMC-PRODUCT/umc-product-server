package com.umc.product.figma.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.adapter.in.web.dto.request.AddFigmaRoutingMentionRequest;
import com.umc.product.figma.adapter.in.web.dto.request.RegisterFigmaRoutingDomainRequest;
import com.umc.product.figma.adapter.in.web.dto.request.UpdateFigmaRoutingDomainRequest;
import com.umc.product.figma.adapter.in.web.dto.request.UpdateFigmaRoutingMentionRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * LLM 분류 결과(domain_key) 단위 라우팅 도메인 + 담당자 mention 관리. 운영진이 화면에서 도메인을 등록하고, 도메인별로 담당자 (Discord role/user) 를 추가/제거한다.
 * <p>
 * ADR-007 에 따라 모든 endpoint 는 SUPER_ADMIN 만 접근 가능하다.
 */
@RestController
@RequestMapping("/api/v1/admin/figma/routing-domains")
@RequiredArgsConstructor
@Tag(name = "Figma | 댓글 분류 카테고리 및 담당자 관리", description = "댓글 분류 도메인과 담당자 멘션을 관리합니다.")
public class FigmaRoutingDomainController {

    private final ManageFigmaRoutingDomainUseCase manageFigmaRoutingDomainUseCase;
    private final GetFigmaRoutingDomainUseCase getFigmaRoutingDomainUseCase;

    @Operation(operationId = "FIGMA-011", summary = "라우팅 도메인 등록")
    @PostMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public RegisterFigmaRoutingDomainResponse registerDomain(
        @RequestBody @Valid RegisterFigmaRoutingDomainRequest request
    ) {
        Long id = manageFigmaRoutingDomainUseCase.registerDomain(request.toCommand());
        return new RegisterFigmaRoutingDomainResponse(id);
    }

    @Operation(operationId = "FIGMA-019", summary = "라우팅 도메인 수정 (설명 · webhook URL · fallback)")
    @PatchMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void updateDomain(
        @PathVariable Long domainId,
        @RequestBody @Valid UpdateFigmaRoutingDomainRequest request
    ) {
        manageFigmaRoutingDomainUseCase.updateDomain(request.toCommand(domainId));
    }

    @Operation(operationId = "FIGMA-012", summary = "라우팅 도메인 삭제")
    @DeleteMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void deleteDomain(@PathVariable Long domainId) {
        manageFigmaRoutingDomainUseCase.deleteDomain(domainId);
    }

    @Operation(operationId = "FIGMA-013", summary = "라우팅 도메인 담당자 멘션 추가")
    @PostMapping("/{domainId}/mentions")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public AddFigmaRoutingMentionResponse addMention(
        @PathVariable Long domainId,
        @RequestBody @Valid AddFigmaRoutingMentionRequest request
    ) {
        Long id = manageFigmaRoutingDomainUseCase.addMention(request.toCommand(domainId));
        return new AddFigmaRoutingMentionResponse(id);
    }

    @Operation(operationId = "FIGMA-020", summary = "담당자 멘션 수정")
    @PatchMapping("/mentions/{mentionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void updateMention(
        @PathVariable Long mentionId,
        @RequestBody @Valid UpdateFigmaRoutingMentionRequest request
    ) {
        manageFigmaRoutingDomainUseCase.updateMention(request.toCommand(mentionId));
    }

    @Operation(operationId = "FIGMA-014", summary = "담당자 멘션 삭제")
    @DeleteMapping("/mentions/{mentionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public void removeMention(@PathVariable Long mentionId) {
        manageFigmaRoutingDomainUseCase.removeMention(mentionId);
    }

    @Operation(operationId = "FIGMA-016", summary = "라우팅 도메인 목록 조회")
    @GetMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public List<FigmaRoutingDomainResponse> listDomains() {
        return getFigmaRoutingDomainUseCase.listDomains().stream()
            .map(FigmaRoutingDomainResponse::from)
            .toList();
    }

    @Operation(operationId = "FIGMA-017", summary = "라우팅 도메인 상세 조회")
    @GetMapping("/{domainId}")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public FigmaRoutingDomainResponse getDomain(@PathVariable Long domainId) {
        return FigmaRoutingDomainResponse.from(getFigmaRoutingDomainUseCase.getDomainById(domainId));
    }

    @Operation(operationId = "FIGMA-018", summary = "라우팅 도메인 담당자 멘션 목록 조회")
    @GetMapping("/{domainId}/mentions")
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.READ)
    public List<FigmaRoutingDomainMentionResponse> listMentions(@PathVariable Long domainId) {
        return getFigmaRoutingDomainUseCase.listMentionsByDomainId(domainId).stream()
            .map(FigmaRoutingDomainMentionResponse::from)
            .toList();
    }
}
