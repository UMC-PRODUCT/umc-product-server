package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.in.RegisterFigmaIntegrationUseCase;
import com.umc.product.figma.application.port.in.dto.RegisterFigmaIntegrationCommand;
import com.umc.product.figma.application.port.out.FigmaOAuthPort;
import com.umc.product.figma.application.port.out.LoadFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.SaveFigmaIntegrationPort;
import com.umc.product.figma.application.port.out.dto.FigmaTokenInfo;
import com.umc.product.figma.domain.FigmaIntegration;
import com.umc.product.figma.domain.exception.FigmaDomainException;
import com.umc.product.figma.domain.exception.FigmaErrorCode;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Figma OAuth 위임 통합의 Command 서비스.
 * - authorization code → token 교환 후 영속화
 * - access token 만료 시 refresh
 * - state 발급/검증 (CSRF 방지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FigmaIntegrationCommandService implements RegisterFigmaIntegrationUseCase {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FigmaOAuthPort figmaOAuthPort;
    private final FigmaTokenCipher figmaTokenCipher;
    private final LoadFigmaIntegrationPort loadFigmaIntegrationPort;
    private final SaveFigmaIntegrationPort saveFigmaIntegrationPort;

    private final Set<String> issuedStates = java.util.Collections.synchronizedSet(new HashSet<>());

    @Override
    public Long register(RegisterFigmaIntegrationCommand command) {
        FigmaTokenInfo tokenInfo = figmaOAuthPort.exchangeCode(command.authorizationCode());

        FigmaIntegration integration = loadFigmaIntegrationPort
            .findByOwnerMemberId(command.ownerMemberId())
            .map(existing -> {
                existing.rotateTokens(
                    figmaTokenCipher.encrypt(tokenInfo.refreshToken()),
                    figmaTokenCipher.encrypt(tokenInfo.accessToken()),
                    tokenInfo.expiresAt(),
                    tokenInfo.scope()
                );
                return existing;
            })
            .orElseGet(() -> FigmaIntegration.of(
                command.ownerMemberId(),
                figmaTokenCipher.encrypt(tokenInfo.refreshToken()),
                figmaTokenCipher.encrypt(tokenInfo.accessToken()),
                tokenInfo.expiresAt(),
                tokenInfo.scope()
            ));

        FigmaIntegration saved = saveFigmaIntegrationPort.save(integration);
        log.info("Figma 통합 등록 완료: ownerMemberId={}, integrationId={}",
            command.ownerMemberId(), saved.getId());
        return saved.getId();
    }

    @Override
    public String issueState() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        issuedStates.add(state);
        return state;
    }

    @Override
    public void verifyState(String state) {
        if (state == null || !issuedStates.remove(state)) {
            throw new FigmaDomainException(FigmaErrorCode.OAUTH_STATE_MISMATCH);
        }
    }

    /**
     * 활성 통합의 access token (평문) 을 반환한다. 만료가 임박하면 refresh 한다.
     */
    public String resolveActiveAccessToken() {
        FigmaIntegration integration = loadFigmaIntegrationPort.findActive()
            .orElseThrow(() -> new FigmaDomainException(FigmaErrorCode.INTEGRATION_NOT_FOUND));

        if (integration.isAccessTokenExpired(Instant.now())) {
            String refreshToken = figmaTokenCipher.decrypt(integration.getRefreshTokenEnc());
            FigmaTokenInfo refreshed = figmaOAuthPort.refresh(refreshToken);
            integration.rotateAccessToken(
                figmaTokenCipher.encrypt(refreshed.accessToken()),
                refreshed.expiresAt()
            );
            saveFigmaIntegrationPort.save(integration);
            return refreshed.accessToken();
        }
        return figmaTokenCipher.decrypt(integration.getAccessTokenEnc());
    }
}
