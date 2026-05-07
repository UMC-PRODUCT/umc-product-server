package com.umc.product.figma.adapter.in.web;

import com.umc.product.figma.adapter.in.web.dto.response.FigmaOAuthAuthorizeResponse;
import com.umc.product.figma.adapter.in.web.dto.response.FigmaOAuthCallbackResponse;
import com.umc.product.figma.application.port.in.RegisterFigmaIntegrationUseCase;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaIntegrationCommand;
import com.umc.product.figma.application.port.out.FigmaOAuthPort;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/figma/oauth")
@RequiredArgsConstructor
@Tag(name = "Figma OAuth | 운영진 위임 인증", description = "운영진이 Figma 댓글 폴링 권한을 위임하기 위한 OAuth 흐름")
public class FigmaOAuthController {

    private final RegisterFigmaIntegrationUseCase registerFigmaIntegrationUseCase;
    private final FigmaOAuthPort figmaOAuthPort;

    /**
     * 동의 화면으로 이동할 authorize URL과 검증용 state 발급.
     * 응답을 받은 클라이언트가 직접 redirect 한다.
     */
    @Operation(summary = "[FIGMA-001] Figma OAuth authorize URL 발급")
    @GetMapping("/start")
    public FigmaOAuthAuthorizeResponse start() {
        String state = registerFigmaIntegrationUseCase.issueState();
        String authorizeUrl = figmaOAuthPort.buildAuthorizeUrl(state);
        return new FigmaOAuthAuthorizeResponse(authorizeUrl, state);
    }

    /**
     * Figma 동의 후 redirect 되어 들어오는 콜백.
     * code 를 token 으로 교환하고 위임 정보를 영속화한다.
     */
    @Operation(summary = "[FIGMA-002] Figma OAuth 콜백 처리")
    @GetMapping("/callback")
    public FigmaOAuthCallbackResponse callback(
        @RequestParam("code") String code,
        @RequestParam("state") String state,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        registerFigmaIntegrationUseCase.verifyState(state);
        Long integrationId = registerFigmaIntegrationUseCase.register(
            new RegisterFigmaIntegrationCommand(memberPrincipal.getMemberId(), code)
        );
        return new FigmaOAuthCallbackResponse(integrationId);
    }
}
