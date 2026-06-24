package com.umc.product.figma.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaOAuthAuthorizeResponse;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaOAuthCallbackResponse;
import com.umc.product.figma.application.port.in.RegisterFigmaIntegrationUseCase;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaIntegrationCommand;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.global.security.annotation.Public;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/figma/oauth")
@RequiredArgsConstructor
@Tag(name = "Figma | OAuth", description = "운영진이 Figma 댓글 폴링 권한을 위임하기 위한 OAuth 흐름")
public class FigmaOAuthController {

    private final RegisterFigmaIntegrationUseCase registerFigmaIntegrationUseCase;

    /**
     * 동의 화면으로 이동할 authorize URL 발급. 인증된 운영진만 호출할 수 있고, 발급된 state 에 호출자의 memberId 가 묶인다. 응답을 받은 클라이언트가 직접 redirect 한다.
     */
    @Operation(operationId = "FIGMA-001", summary = "Figma OAuth 인증 URL 발급")
    @GetMapping
    @CheckAccess(resourceType = ResourceType.FIGMA, permission = PermissionType.MANAGE)
    public FigmaOAuthAuthorizeResponse start(
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        String state = registerFigmaIntegrationUseCase.issueState(memberPrincipal.getMemberId());
        String authorizeUrl = registerFigmaIntegrationUseCase.buildAuthorizeUrl(state);
        return new FigmaOAuthAuthorizeResponse(authorizeUrl, state);
    }

    /**
     * Figma 동의 후 redirect 되어 들어오는 콜백. 브라우저 redirect 라 JWT 가 실리지 않으므로 인증 컨텍스트에 의존하지 않고, /start 에서 발급할 때 묶어 둔 memberId 를
     * state 로부터 복원해 사용한다.
     */
    @Operation(operationId = "FIGMA-002", summary = "Figma OAuth 콜백")
    @GetMapping("/callback")
    @Public
    public FigmaOAuthCallbackResponse callback(
        @RequestParam("code") String code,
        @RequestParam("state") String state
    ) {
        Long ownerMemberId = registerFigmaIntegrationUseCase.consumeState(state);
        Long integrationId = registerFigmaIntegrationUseCase.register(
            new RegisterFigmaIntegrationCommand(ownerMemberId, code)
        );
        return new FigmaOAuthCallbackResponse(integrationId);
    }
}
