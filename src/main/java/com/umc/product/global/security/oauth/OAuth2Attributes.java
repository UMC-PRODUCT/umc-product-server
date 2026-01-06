package com.umc.product.global.security.oauth;

import com.umc.product.member.application.port.in.command.ProcessOAuthLoginCommand;
import com.umc.product.member.domain.OAuthProvider;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2Attributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String nickname;
    private OAuthProvider provider;
    private String providerId;

    public static OAuth2Attributes of(String registrationId,
                                      String userNameAttributeName,
                                      Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(userNameAttributeName, attributes);
            case "kakao" -> ofKakao(userNameAttributeName, attributes);
            case "naver" -> ofNaver(userNameAttributeName, attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private static OAuth2Attributes ofGoogle(String userNameAttributeName,
                                             Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .provider(OAuthProvider.GOOGLE)
                .providerId((String) attributes.get(userNameAttributeName))
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .nickname((String) attributes.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuth2Attributes ofKakao(String userNameAttributeName,
                                            Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuth2Attributes.builder()
                .provider(OAuthProvider.KAKAO)
                .providerId(String.valueOf(attributes.get(userNameAttributeName)))
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuth2Attributes ofNaver(String userNameAttributeName,
                                            Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get(userNameAttributeName);

        return OAuth2Attributes.builder()
                .provider(OAuthProvider.NAVER)
                .providerId((String) response.get("id"))
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .nickname((String) response.get("nickname"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public ProcessOAuthLoginCommand toCommand() {
        return new ProcessOAuthLoginCommand(
                provider,
                providerId,
                email,
                name,
                nickname
        );
    }
}
