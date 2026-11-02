package com.umc.product.community.adapter.in.web;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.umc.product.community.domain.CommunityThreadLinks;
import com.umc.product.global.client.ClientDeviceType;
import com.umc.product.global.config.DeepLinkProperties;
import com.umc.product.global.security.annotation.Public;

import lombok.RequiredArgsConstructor;

/**
 * 커뮤니티 스레드 공유 링크가 앱이 가로채지 못하고 브라우저로 열렸을 때 보여줄 페이지(#1268).
 *
 * <p>App Link/Universal Link 검증을 통과한 기기에서는 이 요청 자체가 OS 레벨에서 가로채져
 * 서버까지 오지 않는다. 여기까지 도달했다는 건 앱이 없거나 검증 전이라는 뜻이라, "앱에서 열기"
 * 시도와 스토어 안내를 함께 보여준다. 단순 리다이렉트 대신 페이지를 쓰는 건, 리다이렉트는 스토어로
 * 바로 튕겨서 "앱에서 열기"를 다시 시도할 여지가 없기 때문이다.</p>
 */
@Controller
@RequiredArgsConstructor
public class CommunityThreadDeepLinkFallbackController {

    private final DeepLinkProperties deepLinkProperties;

    @GetMapping(CommunityThreadLinks.WEB_PATH_TEMPLATE)
    @Public
    public String show(
        @PathVariable Long threadId,
        @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
        Model model
    ) {
        ClientDeviceType platform = ClientDeviceType.fromUserAgent(userAgent);
        DeepLinkProperties.Android android = deepLinkProperties.android();

        model.addAttribute("appDeepLink", appDeepLink(platform, threadId, android));
        model.addAttribute("platform", platform.name());
        model.addAttribute("androidStoreUrl", android.playStoreUrl());
        model.addAttribute("iosStoreUrl", deepLinkProperties.ios().appStoreUrl());
        model.addAttribute("fallbackUrl", deepLinkProperties.fallbackUrl());
        return "community/thread-deep-link";
    }

    // Android는 앱이 없을 때 커스텀 스킴 링크가 무반응이라, 스토어로 넘어가는 intent URL을 쓴다.
    private String appDeepLink(ClientDeviceType platform, Long threadId, DeepLinkProperties.Android android) {
        if (platform == ClientDeviceType.ANDROID) {
            return CommunityThreadLinks.androidIntentUrl(threadId, android.packageName(), android.playStoreUrl());
        }
        return CommunityThreadLinks.appUrl(threadId);
    }
}
