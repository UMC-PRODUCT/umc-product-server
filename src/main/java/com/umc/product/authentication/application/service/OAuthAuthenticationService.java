package com.umc.product.authentication.application.service;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.IdTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.application.port.out.VerifyIdTokenPort;
import com.umc.product.authentication.domain.MemberOAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth 인증 관련 비즈니스 로직을 처리하는 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OAuthAuthenticationService implements OAuthAuthenticationUseCase {

    private final VerifyIdTokenPort verifyIdTokenPort;
    private final LoadMemberOAuthPort loadMemberOAuthPort;
    private final SaveMemberOAuthPort saveMemberOAuthPort;

    @Override
    public Long oAuthLogin(OAuthLoginCommand command) {
        log.info("OAuth 로그인 시도: provider={}, providerId={}",
                command.provider(), command.providerId());

        // 기존 OAuth 연동 정보로 회원 조회
        return loadMemberOAuthPort
                .findByProviderAndProviderId(command.provider(), command.providerId())
                .map(MemberOAuth::getMemberId)
                .orElse(null);  // 없으면 null 반환 (회원가입 필요)
    }

    @Override
    public IdTokenLoginResult idTokenLogin(IdTokenLoginCommand command) {
        log.info("ID 토큰 기반 OAuth 로그인 시도: provider={}", command.provider());

        // 1. ID 토큰 검증 및 사용자 정보 추출 (Port Out 호출)
        OAuth2Attributes oauthAttrs = verifyIdTokenPort.verify(
                command.provider(),
                command.token()
        );

        log.info("OAuth 토큰 검증 성공: provider={}, providerId={}, email={}",
                oauthAttrs.getProvider(),
                oauthAttrs.getProviderId(),
                oauthAttrs.getEmail()
        );

        // 2. 기존 OAuth 연동 정보로 회원 조회
        return loadMemberOAuthPort
                .findByProviderAndProviderId(
                        oauthAttrs.getProvider(),
                        oauthAttrs.getProviderId()
                )
                .map(memberOAuth -> {
                    log.info("기존 회원 로그인 성공: memberId={}", memberOAuth.getMemberId());
                    return IdTokenLoginResult.existingMember(
                            memberOAuth.getMemberId(),
                            oauthAttrs.getProvider(),
                            oauthAttrs.getProviderId(),
                            oauthAttrs.getEmail()
                    );
                })
                .orElseGet(() -> {
                    log.info("신규 회원 - 회원가입 필요: provider={}, providerId={}",
                            oauthAttrs.getProvider(), oauthAttrs.getProviderId());
                    return IdTokenLoginResult.newMember(
                            oauthAttrs.getProvider(),
                            oauthAttrs.getProviderId(),
                            oauthAttrs.getEmail(),
                            oauthAttrs.getName(),
                            oauthAttrs.getNickname()
                    );
                });
    }

    @Override
    public void linkOAuth(LinkOAuthCommand command) {
        // TODO: OAuth 계정 연동 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void unlinkOAuth(UnlinkOAuthCommand command) {
        // TODO: OAuth 계정 연동 해제 구현
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
