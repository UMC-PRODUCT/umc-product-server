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
 * 커뮤니티 스레드 공유 링크가 앱이 가로채지 못하고 브라우저로 열렸을 때 응답하는 페이지(#1268).
 *
 * <p>App Link/Universal Link 검증을 통과한 기기에서는 이 요청 자체가 OS 레벨에서 가로채져
 * 서버까지 오지 않는다. 여기까지 왔다면 앱이 없거나, 인앱 브라우저(카카오톡 등)·주소창 직접 입력처럼
 * OS가 링크를 가로채지 않는 경로다.</p>
 *
 * <p>모바일에서는 버튼 없이 앱 실행을 자동으로 시도하고, 앱이 열리지 않으면 플랫폼 스토어로 이동한다.
 * 서버 302로 스토어에 바로 보내지 않는 이유는, 인앱 브라우저로 들어온 사용자는 앱이 있어도 여기까지
 * 오기 때문에 이미 설치한 앱의 스토어 페이지를 보게 되기 때문이다. 데스크톱 등 앱을 열 수 없는
 * 환경에서는 두 스토어 링크만 보여준다.</p>
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
        String playStoreUrl = deepLinkProperties.android().playStoreUrl();
        String appStoreUrl = deepLinkProperties.ios().appStoreUrl();

        model.addAttribute("platform", platform.name());
        model.addAttribute("autoLaunch", isMobile(platform));
        model.addAttribute("appDeepLink", appDeepLink(platform, threadId));
        model.addAttribute("storeUrl", platform == ClientDeviceType.IOS ? appStoreUrl : playStoreUrl);
        model.addAttribute("androidStoreUrl", playStoreUrl);
        model.addAttribute("iosStoreUrl", appStoreUrl);
        return "community/thread-deep-link";
    }

    private static boolean isMobile(ClientDeviceType platform) {
        return platform == ClientDeviceType.IOS || platform == ClientDeviceType.ANDROID;
    }

    // Android는 앱이 없을 때 커스텀 스킴 링크가 무반응이라, 스토어로 넘어가는 intent URL을 쓴다.
    private String appDeepLink(ClientDeviceType platform, Long threadId) {
        if (platform == ClientDeviceType.ANDROID) {
            DeepLinkProperties.Android android = deepLinkProperties.android();
            return CommunityThreadLinks.androidIntentUrl(threadId, android.packageName(), android.playStoreUrl());
        }
        return CommunityThreadLinks.appUrl(threadId);
    }
}
