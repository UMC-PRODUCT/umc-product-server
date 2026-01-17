package com.umc.product.authentication.application.service;

import com.umc.product.authentication.adapter.in.oauth.OAuth2Attributes;
import com.umc.product.authentication.application.port.in.query.GetMemberOAuthUseCase;
import com.umc.product.authentication.application.port.in.query.dto.MemberOAuthInfo;
import com.umc.product.global.security.MemberPrincipal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class UmcProductOAuth2UserService extends DefaultOAuth2UserService {

    private final GetMemberOAuthUseCase getMemberOAuthUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustonOAuth2UserService {} {}",
                userRequest.getClientRegistration(),
                userRequest.getAdditionalParameters());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Attributes를 파싱하는 역할은 OAuth2Attributes가 담당
        OAuth2Attributes oAuth2Attributes = OAuth2Attributes.of(
                userRequest.getClientRegistration().getRegistrationId(),
                attributes
        );

        // Request에 OAuth 정보 저장 (FailureHandler에서 사용)
        storeOAuthInfoInRequest(oAuth2Attributes);

        // 정보 없으면 UseCase에서 MemberException Throw 함
        MemberOAuthInfo memberOAuthInfo = getMemberOAuthUseCase.getMemberByOAuthInfo(
                oAuth2Attributes.getProvider(),
                oAuth2Attributes.getProviderId()
        );

        return MemberPrincipal.builder()
                .memberId(memberOAuthInfo.memberId())
                .build();
    }

    private void storeOAuthInfoInRequest(OAuth2Attributes oAuth2Attributes) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.getRequest().setAttribute("oauth_email", oAuth2Attributes.getEmail());
            attributes.getRequest().setAttribute("oauth_provider", oAuth2Attributes.getProvider());
            attributes.getRequest().setAttribute("oauth_provider_id", oAuth2Attributes.getProviderId());
        }
    }
}
