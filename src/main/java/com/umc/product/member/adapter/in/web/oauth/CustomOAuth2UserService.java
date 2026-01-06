package com.umc.product.member.adapter.in.web.oauth;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.oauth.OAuth2Attributes;
import com.umc.product.member.application.port.in.command.ProcessOAuthLoginUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final ProcessOAuthLoginUseCase processOAuthLoginUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("=== OAuth2 login started: provider={} ===", registrationId);

        try {
            // 1. OAuth Provider에서 사용자 정보 가져오기
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            log.debug("OAuth2User loaded successfully from provider: {}", registrationId);
            log.debug("OAuth2User attributes: {}", oAuth2User.getAttributes());

            // 2. Provider 정보 추출
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails()
                    .getUserInfoEndpoint()
                    .getUserNameAttributeName();
            log.debug("UserNameAttributeName: {}", userNameAttributeName);

            // 3. OAuth 응답을 Command로 변환
            OAuth2Attributes attributes = OAuth2Attributes.of(
                    registrationId,
                    userNameAttributeName,
                    oAuth2User.getAttributes()
            );
            log.debug("Parsed OAuth2Attributes: provider={}, providerId={}, email={}, name={}",
                    attributes.getProvider(), attributes.getProviderId(),
                    attributes.getEmail(), attributes.getName());

            // 4. UseCase 호출 (회원 조회 또는 가입)
            Long memberId = processOAuthLoginUseCase.processOAuthLogin(attributes.toCommand());
            log.info("OAuth2 login completed: provider={}, memberId={}", registrationId, memberId);

            // 5. MemberPrincipal 반환 (Spring Security가 사용)
            return new MemberPrincipal(
                    memberId,
                    attributes.getEmail(),
                    attributes.getAttributes(),
                    attributes.getNameAttributeKey()
            );
        } catch (Exception ex) {
            log.error("=== OAuth2 login failed: provider={} ===", registrationId, ex);
            log.error("Error type: {}", ex.getClass().getName());
            log.error("Error message: {}", ex.getMessage());
            if (ex.getCause() != null) {
                log.error("Cause: {}", ex.getCause().getMessage());
            }
            throw ex;
        }
    }
}
