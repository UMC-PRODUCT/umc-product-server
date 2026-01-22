package com.umc.product.authentication.application.service;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.global.exception.NotImplementedException;
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

    private final VerifyOAuthTokenPort verifyIdTokenPort;
    private final LoadMemberOAuthPort loadMemberOAuthPort;
    private final SaveMemberOAuthPort saveMemberOAuthPort;

    @Override
    @Transactional(readOnly = true)
    public OAuthTokenLoginResult loginWithOAuth2Attributes(OAuth2Attributes oAuth2Attributes) {
        log.info("OAuth2Attributes 기반 로그인 시도: provider={}, providerId={}",
                oAuth2Attributes.getProvider(), oAuth2Attributes.getProviderId());

        return loadMemberOAuthPort
                // OAuth 정보로 기존 회원이 존재하는지 확인
                .findByProviderAndProviderId(
                        oAuth2Attributes.getProvider(),
                        oAuth2Attributes.getProviderId()
                )
                // 기존 회원이 존재하는지 확인
                .map(memberOAuth -> {
                    log.info("기존 회원 로그인 성공: memberId={}", memberOAuth.getMemberId());
                    return OAuthTokenLoginResult.existingMember(
                            memberOAuth.getMemberId(),
                            oAuth2Attributes.getProvider(),
                            oAuth2Attributes.getProviderId(),
                            oAuth2Attributes.getEmail()
                    );
                })
                // 존재하지 않는 회원인 경우에 대한 처리
                .orElseGet(() -> {
                    log.info("신규 회원 - 회원가입 필요: provider={}, providerId={}",
                            oAuth2Attributes.getProvider(), oAuth2Attributes.getProviderId());
                    return OAuthTokenLoginResult.newMember(
                            oAuth2Attributes.getProvider(),
                            oAuth2Attributes.getProviderId(),
                            oAuth2Attributes.getEmail()
                    );
                });
    }

    @Override
    public OAuthTokenLoginResult accessTokenLogin(AccessTokenLoginCommand command) {
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

        // 2. 공통 비즈니스 로직 재사용
        return loginWithOAuth2Attributes(oauthAttrs);
    }

    @Override
    public Long linkOAuth(LinkOAuthCommand command) {
        // TODO: OAuth 계정 연동 구현
        MemberOAuth memberOAuth = MemberOAuth.builder()
                .memberId(command.memberId())
                .provider(command.provider())
                .providerId(command.providerId())
                .build();

        return saveMemberOAuthPort.save(memberOAuth).getId();
    }

    @Override
    public void unlinkOAuth(UnlinkOAuthCommand command) {
        // TODO: OAuth 계정 연동 해제 구현
        throw new NotImplementedException();
    }
}
