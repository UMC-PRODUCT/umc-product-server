package com.umc.product.authentication.application.service;

import com.umc.product.authentication.domain.OAuthAttributes;
import com.umc.product.authentication.application.port.in.command.OAuthAuthenticationUseCase;
import com.umc.product.authentication.application.port.in.command.dto.AccessTokenLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.AuthorizationCodeLoginCommand;
import com.umc.product.authentication.application.port.in.command.dto.LinkOAuthCommand;
import com.umc.product.authentication.application.port.in.command.dto.OAuthTokenLoginResult;
import com.umc.product.authentication.application.port.in.command.dto.UnlinkOAuthCommand;
import com.umc.product.authentication.application.port.out.LoadMemberOAuthPort;
import com.umc.product.authentication.application.port.out.RevokeOAuthTokenPort;
import com.umc.product.authentication.application.port.out.SaveMemberOAuthPort;
import com.umc.product.authentication.application.port.out.VerifyOAuthTokenPort;
import com.umc.product.authentication.domain.MemberOAuth;
import com.umc.product.authentication.domain.exception.AuthenticationDomainException;
import com.umc.product.authentication.domain.exception.AuthenticationErrorCode;
import com.umc.product.common.domain.enums.OAuthProvider;
import com.umc.product.member.application.port.in.query.GetMemberCredentialUseCase;
import com.umc.product.member.application.port.in.query.dto.MemberCredentialStatusInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final RevokeOAuthTokenPort revokeOAuthTokenPort;
    private final GetMemberCredentialUseCase getMemberCredentialUseCase;

    @Override
    @Transactional(readOnly = true)
    public OAuthTokenLoginResult loginWithOAuthAttributes(OAuthAttributes oAuthAttributes) {
        log.info("OAuthAttributes 기반 로그인 시도: provider={}, providerId={}",
            oAuthAttributes.provider(), oAuthAttributes.providerId());

        return loadMemberOAuthPort
            // OAuth 정보로 기존 회원이 존재하는지 확인
            .findByProviderAndProviderId(
                oAuthAttributes.provider(),
                oAuthAttributes.providerId()
            )
            // 기존 회원이 존재하는지 확인
            .map(memberOAuth -> {
                log.info("기존 회원 로그인 성공: memberId={}", memberOAuth.getMemberId());
                return OAuthTokenLoginResult.existingMember(
                    memberOAuth.getMemberId(),
                    oAuthAttributes.provider(),
                    oAuthAttributes.providerId(),
                    oAuthAttributes.email()
                );
            })
            // 존재하지 않는 회원인 경우에 대한 처리
            .orElseGet(() -> {
                log.info("신규 회원 - 회원가입 필요: provider={}, providerId={}",
                    oAuthAttributes.provider(), oAuthAttributes.providerId());
                return OAuthTokenLoginResult.newMember(
                    oAuthAttributes.provider(),
                    oAuthAttributes.providerId(),
                    oAuthAttributes.email()
                );
            });
    }

    @Override
    public OAuthTokenLoginResult accessTokenLogin(AccessTokenLoginCommand command) {
        log.info("ID 토큰 기반 OAuth 로그인 시도: provider={}", command.provider());

        // 1. ID 토큰 검증 및 사용자 정보 추출 (Port Out 호출)
        OAuthAttributes oauthAttrs = verifyIdTokenPort.verify(
            command.provider(),
            command.token()
        );

        log.info("OAuth 토큰 검증 성공: provider={}, providerId={}, email={}",
            oauthAttrs.provider(),
            oauthAttrs.providerId(),
            oauthAttrs.email()
        );

        // 2. 공통 비즈니스 로직 재사용
        return loginWithOAuthAttributes(oauthAttrs);
    }

    @Override
    public OAuthTokenLoginResult authorizationCodeLogin(AuthorizationCodeLoginCommand command) {
        log.info("Authorization Code 기반 OAuth 로그인 시도: provider={}", command.provider());

        // 1. Authorization Code 교환 및 사용자 정보 추출 (Port Out 호출)
        OAuthAttributes oauthAttrs = verifyIdTokenPort.verifyAuthorizationCode(
            command.provider(),
            command.authorizationCode(),
            command.redirectUri()
        );

        log.info("OAuth Authorization Code 교환 성공: provider={}, providerId={}, email={}",
            oauthAttrs.provider(),
            oauthAttrs.providerId(),
            oauthAttrs.email()
        );

        // 2. 공통 비즈니스 로직 재사용
        return loginWithOAuthAttributes(oauthAttrs);
    }

    @Override
    public Long linkOAuth(LinkOAuthCommand command) {
        // 1. 동일한 OAuth 계정이 이미 연동되어 있는지 확인
        loadMemberOAuthPort.findByProviderAndProviderId(command.provider(), command.providerId())
            .ifPresent(existing -> {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_ALREADY_LINKED);
            });

        // 2. 해당 회원이 이미 같은 provider로 연동했는지 확인 (선택적)
        // 일단 나중에 적용하기 위해서 주석 처리
        loadMemberOAuthPort.findByMemberIdAndProvider(command.memberId(), command.provider())
            .ifPresent(existing -> {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_PROVIDER_ALREADY_LINKED);
            });

        MemberOAuth created = saveMemberOAuthPort.save(LinkOAuthCommand.toEntity(command));

        return created.getId();
    }

    @Override
    public List<Long> linkOAuthBulk(List<LinkOAuthCommand> commands) {
        // provider별로 그룹핑하여 벌크 검증
        Map<OAuthProvider, List<LinkOAuthCommand>> commandsByProvider = commands.stream()
            .collect(Collectors.groupingBy(LinkOAuthCommand::provider));

        commandsByProvider.forEach((provider, providerCommands) -> {
            // 1. 동일한 OAuth 계정이 이미 연동되어 있는지 벌크 확인
            List<String> providerIds = providerCommands.stream()
                .map(LinkOAuthCommand::providerId)
                .toList();

            List<MemberOAuth> alreadyLinked =
                loadMemberOAuthPort.findAllByProviderAndProviderIdIn(provider, providerIds);
            if (!alreadyLinked.isEmpty()) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_ALREADY_LINKED);
            }

            // 2. 해당 회원이 이미 같은 provider로 연동했는지 벌크 확인
            List<Long> memberIds = providerCommands.stream()
                .map(LinkOAuthCommand::memberId)
                .toList();

            List<MemberOAuth> alreadyLinkedByMember =
                loadMemberOAuthPort.findAllByMemberIdInAndProvider(memberIds, provider);
            if (!alreadyLinkedByMember.isEmpty()) {
                throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_PROVIDER_ALREADY_LINKED);
            }
        });

        // 3. 벌크 저장
        List<MemberOAuth> entities = commands.stream()
            .map(LinkOAuthCommand::toEntity)
            .toList();

        List<MemberOAuth> saved = saveMemberOAuthPort.saveAll(entities);

        return saved.stream()
            .map(MemberOAuth::getId)
            .toList();
    }

    @Override
    public void unlinkOAuth(UnlinkOAuthCommand command) {
        MemberOAuth memberOAuth = loadMemberOAuthPort.findByMemberOAuthId(command.memberOAuthId())
            .orElseThrow(() -> new AuthenticationDomainException(AuthenticationErrorCode.MEMBER_OAUTH_NOT_FOUND));

        memberOAuth.throwIfNotValidMember(command.memberId());

        if (!command.isWithdrawal()) {
            validateLoginMethodInvariant(command.memberId());
        }

        // Provider별 토큰 revoke / 연결 해제
        revokeProviderToken(memberOAuth, command);

        saveMemberOAuthPort.delete(memberOAuth);
    }

    private void validateLoginMethodInvariant(Long memberId) {
        // Member row lock 이후 OAuth 개수를 확인해 같은 회원의 동시 OAuth 해제 요청을 직렬화한다.
        MemberCredentialStatusInfo credentialStatus =
            getMemberCredentialUseCase.getCredentialStatusForUpdate(memberId);
        if (credentialStatus.hasCredential()) {
            return;
        }

        List<MemberOAuth> linkedOAuth = loadMemberOAuthPort.findAllByMemberId(memberId);
        if (linkedOAuth.size() <= 1) {
            throw new AuthenticationDomainException(AuthenticationErrorCode.OAUTH_CANNOT_UNLINK_LAST_PROVIDER);
        }
    }

    private void revokeProviderToken(MemberOAuth memberOAuth, UnlinkOAuthCommand command) {
        switch (memberOAuth.getProvider()) {
            case APPLE -> {
                if (memberOAuth.getAppleRefreshToken() != null && memberOAuth.getAppleClientId() != null) {
                    revokeOAuthTokenPort.revokeAppleToken(
                            memberOAuth.getAppleRefreshToken(),
                            memberOAuth.getAppleClientId()
                    );
                } else {
                    log.warn("[Apple 계정 연동 해제] refresh token 또는 client_id가 없어 revoke를 skip합니다: "
                                    + "memberId={} memberOAuthId={} hasRefreshToken={} hasClientId={}",
                            memberOAuth.getMemberId(), memberOAuth.getId(),
                            memberOAuth.getAppleRefreshToken() != null,
                            memberOAuth.getAppleClientId() != null);
                }
            }
            case KAKAO -> {
                if (command.kakaoAccessToken() != null) {
                    revokeOAuthTokenPort.revokeKakaoToken(command.kakaoAccessToken());
                } else {
                    log.warn("[Kakao 계정 연동 해제] access token이 전달되지 않아 revoke를 skip합니다: memberId={} memberOAuthId={}",
                        memberOAuth.getMemberId(), memberOAuth.getId());
                }
            }
            case GOOGLE -> {
                if (command.googleAccessToken() != null) {
                    revokeOAuthTokenPort.revokeGoogleToken(command.googleAccessToken());
                } else {
                    log.warn("[Google 계정 연동 해제] access token이 전달되지 않아 revoke를 skip합니다: memberId={} memberOAuthId={}",
                        memberOAuth.getMemberId(), memberOAuth.getId());
                }
            }
        }
    }

    @Override
    public void updateAppleRefreshToken(OAuthProvider provider, String providerId, String refreshToken,
                                        String clientId) {
        MemberOAuth memberOAuth = loadMemberOAuthPort.findByProviderAndProviderId(provider, providerId)
            .orElseThrow(() -> new AuthenticationDomainException(AuthenticationErrorCode.MEMBER_OAUTH_NOT_FOUND));

        memberOAuth.updateAppleCredentials(refreshToken, clientId);
        saveMemberOAuthPort.save(memberOAuth);
    }
}
