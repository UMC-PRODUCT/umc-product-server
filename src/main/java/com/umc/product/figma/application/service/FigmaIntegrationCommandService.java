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
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Figma OAuth 위임 통합의 Command 서비스.
 * - authorization code → token 교환 후 영속화
 * - access token 만료 시 refresh
 * - state 발급/검증은 {@link FigmaOAuthStateStore} 에 위임 (memberId 바인딩 + single-use)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FigmaIntegrationCommandService implements RegisterFigmaIntegrationUseCase {

    private final FigmaOAuthPort figmaOAuthPort;
    private final FigmaTokenCipher figmaTokenCipher;
    private final FigmaOAuthStateStore figmaOAuthStateStore;
    private final LoadFigmaIntegrationPort loadFigmaIntegrationPort;
    private final SaveFigmaIntegrationPort saveFigmaIntegrationPort;

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
    public String issueState(Long ownerMemberId) {
        return figmaOAuthStateStore.issue(ownerMemberId);
    }

    @Override
    public Long consumeState(String state) {
        return figmaOAuthStateStore.consume(state);
    }

    /**
     * 활성 통합의 access token (평문) 을 반환한다. 만료가 임박하면 refresh 한다.
     * read-only 트랜잭션(예: preview) 에서 호출되더라도 token refresh 가 가능하도록 REQUIRES_NEW 로 분리한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
